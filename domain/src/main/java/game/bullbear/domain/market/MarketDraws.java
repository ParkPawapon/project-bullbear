package game.bullbear.domain.market;

/** Returns an unbiased integer in [0, bound) at a stable, round-local address. */
@FunctionalInterface
public interface MarketDraws {
    enum Channel {
        REGIME_TRANSITION,
        COMMON_NOISE,
        STOCK_NOISE,
        EVENT_OCCURRENCE,
        EVENT_SELECTION
    }

    int draw(long ordinal, Channel channel, String subject, int drawIndex, int bound);
}
