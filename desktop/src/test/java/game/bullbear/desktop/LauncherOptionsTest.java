package game.bullbear.desktop;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LauncherOptionsTest {
    @Test
    void noArgumentSelectsInteractiveLaunch() {
        assertEquals(LauncherOptions.INTERACTIVE, LauncherOptions.parse(new String[0]));
    }

    @Test
    void helpSelectsTheNonGraphicsPath() {
        assertEquals(LauncherOptions.HELP, LauncherOptions.parse(new String[] {"--help"}));
    }

    @Test
    void smokeTestSelectsTheGraphicsSmokePath() {
        assertEquals(
                LauncherOptions.SMOKE_TEST, LauncherOptions.parse(new String[] {"--smoke-test"}));
    }

    @Test
    void reportsAnUnsupportedArgument() {
        var failure =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> LauncherOptions.parse(new String[] {"--headless"}));
        assertTrue(failure.getMessage().contains("--headless"));
    }

    @ParameterizedTest
    @CsvSource({
        "--help, --help",
        "--smoke-test, --smoke-test",
        "--help, --smoke-test",
        "--smoke-test, --help"
    })
    void rejectsDuplicateOrConflictingOptions(String first, String second) {
        String[] arguments = {first, second};
        assertThrows(IllegalArgumentException.class, () -> LauncherOptions.parse(arguments));
        assertArrayEquals(new String[] {first, second}, arguments);
    }

    @Test
    void doesNotEditValidCallerArguments() {
        String[] arguments = {"--smoke-test"};
        LauncherOptions.parse(arguments);
        assertArrayEquals(new String[] {"--smoke-test"}, arguments);
    }
}
