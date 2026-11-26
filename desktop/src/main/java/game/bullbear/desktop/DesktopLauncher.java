package game.bullbear.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import game.bullbear.application.catalog.GameDesignCatalog;
import game.bullbear.presentation.FoundationGame;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class DesktopLauncher {
    private static final String USAGE =
            "Usage: bullbear [--help | --version | --smoke-test | --diagnostics | --thai-preview]";

    private DesktopLauncher() {}

    public static void main(String[] args) {
        LauncherOptions options;
        try {
            options = LauncherOptions.parse(args);
        } catch (IllegalArgumentException invalid) {
            System.err.println("Invalid launcher arguments");
            System.err.println(USAGE);
            System.exit(2);
            return;
        }
        if (options == LauncherOptions.HELP) {
            System.out.println(USAGE);
            System.out.println("  --smoke-test  Render a real frame and exit.");
            return;
        }
        if (options == LauncherOptions.VERSION) {
            System.out.println("Bullbear " + version());
            return;
        }

        System.exit(run(options));
    }

    private static int run(LauncherOptions options) {
        final DesktopDirectories directories;
        try {
            directories = DesktopDirectories.resolve();
        } catch (IllegalArgumentException | NullPointerException | SecurityException invalid) {
            System.err.println("Bullbear could not resolve its application directories.");
            return 1;
        }
        try (var log = new DesktopDiagnostics(directories.state())) {
            log.info(
                    "Starting Bullbear "
                            + version()
                            + " with Java "
                            + Runtime.version()
                            + " on "
                            + System.getProperty("os.name")
                            + " "
                            + System.getProperty("os.arch"));
            DesktopSettings settings = DesktopSettings.DEFAULT;
            try {
                settings = new SettingsStore(directories.config()).loadOrCreate();
            } catch (IOException | SecurityException invalid) {
                log.warn("Settings unavailable or invalid. Using defaults for this session.");
            }
            if (!settings.locale().equals(Locale.ENGLISH)) {
                log.warn("Thai labels are pending visual validation. Using the English shell.");
            }
            if (settings.audio()) {
                log.info("Audio preference retained. This shell has no audio assets.");
            }
            boolean smokeTest = options == LauncherOptions.SMOKE_TEST;
            var game = new FoundationGame(new GameDesignCatalog(), smokeTest);
            try {
                new Lwjgl3Application(game, windowConfiguration(settings));
                if (smokeTest) {
                    if (!game.smokeFrameRendered()) {
                        throw new IllegalStateException(
                                "Window closed before the smoke frame rendered.");
                    }
                    System.out.println("FOUNDATION_SMOKE_OK");
                }
                log.info("Bullbear shut down normally");
                return 0;
            } catch (RuntimeException | LinkageError failure) {
                log.reportCrash(failure);
                System.err.println(
                        "Bullbear could not start or continue. Check the display, "
                                + "OpenGL driver and matching Java architecture.");
                return 1;
            }
        }
    }

    static String version() {
        try (var resource = DesktopLauncher.class.getResourceAsStream("/bullbear-version.txt")) {
            if (resource == null) {
                throw new IllegalStateException("Missing packaged version");
            }
            return new String(resource.readAllBytes(), StandardCharsets.UTF_8).strip();
        } catch (IOException invalid) {
            throw new IllegalStateException("Cannot read packaged version", invalid);
        }
    }

    private static Lwjgl3ApplicationConfiguration windowConfiguration(DesktopSettings settings) {
        var config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Bullbear - Foundation");
        config.setWindowedMode(settings.width(), settings.height());
        config.setWindowSizeLimits(DesktopSettings.MIN_WIDTH, DesktopSettings.MIN_HEIGHT, -1, -1);
        config.setHdpiMode(HdpiMode.Logical);
        config.setResizable(true);
        config.useVsync(true);
        config.setForegroundFPS(60);
        config.disableAudio(true);
        return config;
    }
}
