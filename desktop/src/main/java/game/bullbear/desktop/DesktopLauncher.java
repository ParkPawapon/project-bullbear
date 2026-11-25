package game.bullbear.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import game.bullbear.application.catalog.GameDesignCatalog;
import game.bullbear.presentation.FoundationGame;
import game.bullbear.presentation.theme.PixelTheme;

public final class DesktopLauncher {
    private static final String USAGE = "Usage: bullbear [--help | --smoke-test]";

    private DesktopLauncher() {}

    public static void main(String[] args) {
        LauncherOptions options;
        try {
            options = LauncherOptions.parse(args);
        } catch (IllegalArgumentException invalid) {
            System.err.println(invalid.getMessage());
            System.err.println(USAGE);
            System.exit(2);
            return;
        }
        if (options == LauncherOptions.HELP) {
            System.out.println(USAGE);
            System.out.println("  --smoke-test  Render a real frame and exit.");
            return;
        }

        boolean smokeTest = options == LauncherOptions.SMOKE_TEST;
        var game = new FoundationGame(new GameDesignCatalog(), smokeTest);
        try {
            new Lwjgl3Application(game, windowConfiguration());
            if (smokeTest) {
                if (!game.smokeFrameRendered()) {
                    throw new IllegalStateException(
                            "Window closed before the smoke frame rendered.");
                }
                System.out.println("FOUNDATION_SMOKE_OK");
            }
        } catch (RuntimeException | LinkageError failure) {
            System.err.println(
                    "Bullbear failed on "
                            + System.getProperty("os.name")
                            + " ("
                            + System.getProperty("os.arch")
                            + "). Check the display, OpenGL driver, "
                            + "and matching Java runtime architecture.");
            throw failure;
        }
    }

    private static Lwjgl3ApplicationConfiguration windowConfiguration() {
        var config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Bullbear - Foundation");
        config.setWindowedMode(PixelTheme.CANVAS_WIDTH, PixelTheme.CANVAS_HEIGHT);
        config.setWindowSizeLimits(800, 450, -1, -1);
        config.setResizable(true);
        config.useVsync(true);
        config.setForegroundFPS(60);
        config.disableAudio(true);
        return config;
    }
}
