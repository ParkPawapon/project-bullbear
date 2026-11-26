package game.bullbear.domain.game;

/** Expected rejection of a well-formed command against the current run. */
public final class GameRejection extends IllegalStateException {
    private final Reason reason;

    GameRejection(Reason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }

    public enum Reason {
        WRONG_PHASE,
        STALE_INPUT,
        INSUFFICIENT_COIN,
        INVENTORY_FULL,
        DUPLICATE_CHARM
    }
}
