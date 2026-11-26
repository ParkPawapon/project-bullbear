package game.bullbear.domain.game;

/** Nonnegative whole Coin. Results use signed differences instead of balance values. */
public record CoinAmount(long units) implements Comparable<CoinAmount> {
    public static final CoinAmount ZERO = new CoinAmount(0);

    public CoinAmount {
        if (units < 0) {
            throw new IllegalArgumentException("Coin cannot be negative");
        }
    }

    public CoinAmount plus(CoinAmount other) {
        return new CoinAmount(Math.addExact(units, other.units));
    }

    public CoinAmount minus(CoinAmount other) {
        return new CoinAmount(Math.subtractExact(units, other.units));
    }

    @Override
    public int compareTo(CoinAmount other) {
        return Long.compare(units, other.units);
    }
}
