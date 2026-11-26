package game.bullbear.desktop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DesktopDiagnosticsTest {
    @TempDir Path state;

    @Test
    void recordsBoundedCauseWithoutExceptionMessagesAndSuppressesDuplicates() throws IOException {
        Path crashFile = state.resolve("logs/last-crash.txt");
        try (var diagnostics = new DesktopDiagnostics(state)) {
            diagnostics.reportCrash(
                    new IllegalStateException(
                            "private fixture path", new IOException("private fixture data")));
            String saved = Files.readString(crashFile);
            assertTrue(saved.contains("java.lang.IllegalStateException"));
            assertTrue(saved.contains("java.io.IOException"));
            assertTrue(saved.length() < 10_000);
            assertFalse(saved.contains("private fixture"));
            diagnostics.reportCrash(new IllegalArgumentException("repeat"));
            assertEquals(saved, Files.readString(crashFile));
        }
    }

    @Test
    void unavailableStateDoesNotHideTheOriginalFailure() throws IOException {
        Path unavailable = Files.writeString(state.resolve("unavailable"), "original");
        var failure = new IllegalStateException("original", new IOException("cause"));
        try (var diagnostics = new DesktopDiagnostics(unavailable)) {
            diagnostics.reportCrash(failure);
        }
        assertEquals("cause", failure.getCause().getMessage());
        assertEquals("original", Files.readString(unavailable));
    }
}
