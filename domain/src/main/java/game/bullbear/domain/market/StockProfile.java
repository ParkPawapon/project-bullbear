package game.bullbear.domain.market;

public record StockProfile(
        int ownAmplitudeBps,
        int commonBeta,
        int moodBeta,
        int eventSensitivity,
        int momentumWeight) {
    public StockProfile {
        MarketMath.requireRange(ownAmplitudeBps, 0, 5000, "Stock noise amplitude");
        MarketMath.requireRange(commonBeta, 0, 2000, "Common beta");
        MarketMath.requireRange(moodBeta, 0, 2000, "Mood beta");
        MarketMath.requireRange(eventSensitivity, 0, 2000, "Event sensitivity");
        MarketMath.requireRange(momentumWeight, 0, 500, "Momentum weight");
    }
}
