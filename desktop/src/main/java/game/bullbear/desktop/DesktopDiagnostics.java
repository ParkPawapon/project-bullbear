package game.bullbear.desktop;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

final class DesktopDiagnostics implements AutoCloseable {
    private static final int LOG_LIMIT = 64 * 1024;
    private final Logger logger = Logger.getAnonymousLogger();
    private final Path directory;
    private final FileHandler file;
    private boolean crashReported;

    DesktopDiagnostics(Path stateDirectory) {
        directory = stateDirectory.resolve("logs");
        var formatter =
                new Formatter() {
                    @Override
                    public String format(LogRecord record) {
                        return record.getInstant()
                                + " "
                                + record.getLevel()
                                + " "
                                + record.getMessage()
                                + "\n";
                    }
                };
        logger.setUseParentHandlers(false);
        FileHandler handler = null;
        try {
            prepareDirectory();
            String pattern = directory.toString().replace("%", "%%") + "/bullbear-%g.log";
            handler = new FileHandler(pattern, LOG_LIMIT, 3, true);
            handler.setFormatter(formatter);
            handler.setEncoding("UTF-8");
            logger.addHandler(handler);
        } catch (IOException | SecurityException unavailable) {
            if (handler != null) {
                handler.close();
            }
            handler = null;
            System.err.println("Bullbear local logging unavailable");
        }
        file = handler;
    }

    void info(String message) {
        logger.info(message);
    }

    void warn(String message) {
        logger.warning(message);
    }

    void reportCrash(Throwable failure) {
        if (crashReported) {
            return;
        }
        crashReported = true;
        StringBuilder report =
                new StringBuilder("Bullbear ")
                        .append(DesktopLauncher.version())
                        .append(' ')
                        .append(Instant.now())
                        .append("\nOS: ")
                        .append(System.getProperty("os.name"))
                        .append(' ')
                        .append(System.getProperty("os.arch"))
                        .append("\nJava: ")
                        .append(Runtime.version())
                        .append('\n');
        Throwable current = failure;
        for (int cause = 0; cause < 4 && current != null; cause++, current = current.getCause()) {
            report.append(current.getClass().getName()).append('\n');
            var frames = current.getStackTrace();
            for (int frame = 0; frame < Math.min(12, frames.length); frame++) {
                var entry = frames[frame];
                report.append("  ")
                        .append(entry.getClassName())
                        .append('.')
                        .append(entry.getMethodName())
                        .append(':')
                        .append(entry.getLineNumber())
                        .append('\n');
            }
        }
        logger.log(
                Level.SEVERE,
                "Fatal startup or runtime failure: " + failure.getClass().getSimpleName());
        try {
            prepareDirectory();
            Files.writeString(
                    directory.resolve("last-crash.txt"),
                    report,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE,
                    LinkOption.NOFOLLOW_LINKS);
        } catch (IOException | SecurityException unavailable) {
            System.err.println("Bullbear local crash report unavailable");
        }
    }

    private void prepareDirectory() throws IOException {
        if (Files.isSymbolicLink(directory) || Files.isSymbolicLink(directory.getParent())) {
            throw new IOException("Symbolic links are disallowed for logs");
        }
        Files.createDirectories(directory);
    }

    @Override
    public void close() {
        if (file != null) {
            logger.removeHandler(file);
            file.close();
        }
    }
}
