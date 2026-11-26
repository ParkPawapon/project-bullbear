package game.bullbear.desktop;

enum LauncherOptions {
    INTERACTIVE,
    HELP,
    VERSION,
    DIAGNOSTICS,
    THAI_PREVIEW,
    SMOKE_TEST;

    static LauncherOptions parse(String[] args) {
        if (args.length == 0) {
            return INTERACTIVE;
        }
        if (args.length > 1) {
            throw new IllegalArgumentException("Supply at most one option.");
        }
        return switch (args[0]) {
            case "--help" -> HELP;
            case "--version" -> VERSION;
            case "--diagnostics" -> DIAGNOSTICS;
            case "--thai-preview" -> THAI_PREVIEW;
            case "--smoke-test" -> SMOKE_TEST;
            default -> throw new IllegalArgumentException("Unknown option: " + args[0]);
        };
    }
}
