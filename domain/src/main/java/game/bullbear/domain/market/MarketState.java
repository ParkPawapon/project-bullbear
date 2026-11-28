package game.bullbear.domain.market;

/** Opaque immutable state created only by the market engine. */
public final class MarketState {
    private final String rulesFingerprint;
    private final MarketSnapshot snapshot;

    MarketState(String rulesFingerprint, MarketSnapshot snapshot) {
        this.rulesFingerprint = rulesFingerprint;
        this.snapshot = snapshot;
    }

    String rulesFingerprint() {
        return rulesFingerprint;
    }

    public MarketSnapshot snapshot() {
        return snapshot;
    }
}
