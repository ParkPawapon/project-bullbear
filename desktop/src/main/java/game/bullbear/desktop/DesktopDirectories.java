package game.bullbear.desktop;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

record DesktopDirectories(Path config, Path state) {
    static DesktopDirectories resolve() {
        return resolve(
                System.getProperty("os.name"),
                Path.of(System.getProperty("user.home")),
                System.getenv());
    }

    static DesktopDirectories resolve(String os, Path home, Map<String, String> environment) {
        if (!home.isAbsolute()) {
            throw new IllegalArgumentException("An absolute user home is required");
        }
        String platform = os.toLowerCase(Locale.ROOT);
        if (platform.startsWith("windows")) {
            Path root =
                    absoluteOr(environment.get("LOCALAPPDATA"), home.resolve("AppData/Local"))
                            .resolve("Bullbear");
            return new DesktopDirectories(root, root);
        }
        if (platform.startsWith("mac")) {
            Path root = home.resolve("Library/Application Support/Bullbear");
            return new DesktopDirectories(root, root);
        }
        return new DesktopDirectories(
                absoluteOr(environment.get("XDG_CONFIG_HOME"), home.resolve(".config"))
                        .resolve("bullbear"),
                absoluteOr(environment.get("XDG_STATE_HOME"), home.resolve(".local/state"))
                        .resolve("bullbear"));
    }

    private static Path absoluteOr(String value, Path fallback) {
        if (value != null && !value.isBlank()) {
            try {
                Path path = Path.of(value);
                if (path.isAbsolute()) {
                    return path.normalize();
                }
            } catch (InvalidPathException ignored) {
                return fallback;
            }
        }
        return fallback;
    }
}
