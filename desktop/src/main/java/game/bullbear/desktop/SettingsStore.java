package game.bullbear.desktop;

import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

final class SettingsStore {
    private static final String FORMAT_VERSION = "1";
    private static final int LIMIT_BYTES = 4096;
    private static final Set<String> KEYS =
            Set.of("formatVersion", "width", "height", "locale", "audio", "volume", "diagnostics");
    private final Path directory;
    private final Path file;

    SettingsStore(Path directory) {
        if (!directory.isAbsolute()) {
            throw new IllegalArgumentException("Settings directory must be absolute");
        }
        this.directory = directory.normalize();
        file = this.directory.resolve("settings.properties");
    }

    DesktopSettings loadOrCreate() throws IOException {
        try {
            return read();
        } catch (NoSuchFileException missing) {
            save(DesktopSettings.DEFAULT);
            return DesktopSettings.DEFAULT;
        }
    }

    DesktopSettings read() throws IOException {
        rejectLinks();
        byte[] bytes;
        try (var source = Files.newInputStream(file, LinkOption.NOFOLLOW_LINKS)) {
            bytes = source.readNBytes(LIMIT_BYTES + 1);
        }
        if (bytes.length > LIMIT_BYTES) {
            throw new IOException("Settings exceed the size limit");
        }
        String text = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
        var values = new Properties();
        try {
            values.load(new StringReader(text));
            if (!FORMAT_VERSION.equals(values.getProperty("formatVersion"))) {
                throw new IOException("Unsupported settings format");
            }
            if (!KEYS.equals(values.stringPropertyNames())) {
                throw new IOException("Settings keys do not match format version 1");
            }
            return new DesktopSettings(
                    Integer.parseInt(values.getProperty("width")),
                    Integer.parseInt(values.getProperty("height")),
                    Locale.forLanguageTag(values.getProperty("locale")),
                    parseBoolean(values.getProperty("audio")),
                    Float.parseFloat(values.getProperty("volume")),
                    parseBoolean(values.getProperty("diagnostics")));
        } catch (IllegalArgumentException invalid) {
            throw new IOException("Invalid settings values", invalid);
        }
    }

    void save(DesktopSettings settings) throws IOException {
        rejectLinks();
        Files.createDirectories(directory);
        Path temp = Files.createTempFile(directory, ".settings-", ".tmp");
        try {
            Files.writeString(
                    temp,
                    String.format(
                            Locale.ROOT,
                            """
                    formatVersion=%s
                    width=%d
                    height=%d
                    locale=%s
                    audio=%s
                    volume=%s
                    diagnostics=%s
                    """,
                            FORMAT_VERSION,
                            settings.width(),
                            settings.height(),
                            settings.locale().toLanguageTag(),
                            settings.audio(),
                            Float.toString(settings.volume()),
                            settings.diagnostics()),
                    StandardCharsets.UTF_8);
            rejectLinks();
            Files.move(
                    temp,
                    file,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | RuntimeException failed) {
            try {
                Files.deleteIfExists(temp);
            } catch (IOException cleanup) {
                failed.addSuppressed(cleanup);
            }
            throw failed;
        }
    }

    private void rejectLinks() throws IOException {
        if (Files.isSymbolicLink(directory) || Files.isSymbolicLink(file)) {
            throw new IOException("Symbolic links are disallowed for settings");
        }
    }

    private static boolean parseBoolean(String value) {
        return switch (value) {
            case "true" -> true;
            case "false" -> false;
            default -> throw new IllegalArgumentException("Expected a boolean");
        };
    }
}
