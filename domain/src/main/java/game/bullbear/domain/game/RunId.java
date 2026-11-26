package game.bullbear.domain.game;

/** Positive caller-supplied session identity. */
public record RunId(long value) {
    public RunId {
        if (value <= 0) {
            throw new IllegalArgumentException("Run ID must be positive");
        }
    }
}
