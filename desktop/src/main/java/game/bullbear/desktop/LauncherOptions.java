package game.bullbear.desktop;

enum LauncherOptions {
    INTERACTIVE,
    HELP,
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
            case "--smoke-test" -> SMOKE_TEST;
            default -> throw new IllegalArgumentException("Unknown option: " + args[0]);
        };
    }
}
