package game.bullbear.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SettingsStoreTest {
    @TempDir Path temporary;

    @Test
    void createsDefaultsAndRoundTripsValidatedOptions() throws IOException {
        var store = new SettingsStore(temporary.resolve("local prefs"));
        assertEquals(DesktopSettings.DEFAULT, store.loadOrCreate());
        var preferences = new DesktopSettings(960, 540, DesktopSettings.THAI, true, 0.4f, true);
        store.save(preferences);
        assertEquals(preferences, store.loadOrCreate());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "formatVersion=2\n",
                "formatVersion=1\nwidth=960\n",
                "formatVersion=1\nwidth=960\nheight=540\nlocale=en\naudio=true\nvolume=NaN\ndiagnostics=false\n",
                "formatVersion=1\nwidth=960\nheight=540\nlocale=en\naudio=true\nvolume=0.5\ndiagnostics=false\nother=x\n",
                "formatVersion=1\nwidth=960\nheight=540\nlocale=fr\naudio=true\nvolume=0.5\ndiagnostics=false\n",
                "formatVersion=1\nwidth=1\nheight=540\nlocale=en\naudio=true\nvolume=0.5\ndiagnostics=false\n",
                "\\uINVALID"
            })
    void rejectsUnreadableOrFutureSettingsAndPreservesOriginal(String content) throws IOException {
        Path file = temporary.resolve("settings.properties");
        Files.writeString(file, content);
        assertThrows(IOException.class, () -> new SettingsStore(temporary).loadOrCreate());
        assertEquals(content, Files.readString(file));
    }

    @Test
    void ignoresRelativeEnvironmentPathsAndSeparatesLinuxState() {
        var directories =
                DesktopDirectories.resolve(
                        "Linux",
                        temporary,
                        Map.of(
                                "XDG_CONFIG_HOME",
                                "../outside",
                                "XDG_STATE_HOME",
                                temporary.resolve("logs root").toString()));
        assertEquals(temporary.resolve(".config/bullbear"), directories.config());
        assertEquals(temporary.resolve("logs root/bullbear"), directories.state());
        assertThrows(IllegalArgumentException.class, () -> new SettingsStore(Path.of("../escape")));
    }

    @Test
    void failedAtomicReplacementKeepsExistingTargetAndRemovesTemporaryFile() throws IOException {
        var target = Files.createDirectory(temporary.resolve("settings.properties"));
        Files.writeString(target.resolve("existing"), "keep");
        var store = new SettingsStore(temporary);
        assertThrows(IOException.class, () -> store.save(DesktopSettings.DEFAULT));
        assertEquals("keep", Files.readString(target.resolve("existing")));
        try (var entries = Files.list(temporary)) {
            assertFalse(entries.anyMatch(file -> file.getFileName().toString().endsWith(".tmp")));
        }
    }

    @Test
    void choosesPlatformDirectoriesWithoutCreatingThem() {
        var windows = DesktopDirectories.resolve("Windows 11", temporary, Map.of());
        var mac = DesktopDirectories.resolve("Mac OS X", temporary, Map.of());
        assertEquals(temporary.resolve("AppData/Local/Bullbear"), windows.config());
        assertEquals(temporary.resolve("Library/Application Support/Bullbear"), mac.config());
        assertTrue(Files.notExists(mac.config()));
    }
}
