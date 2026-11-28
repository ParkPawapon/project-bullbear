package game.bullbear.domain.market;

import java.math.BigInteger;

final class MarketMath {
    static final int BASIS = 10_000;
    static final long MIN_INDEX = 10_000;
    static final long MAX_INDEX = 1_000_000_000_000_000_000L;
    private static final BigInteger INDEX_DIVISOR = BigInteger.valueOf(BASIS);
    private static final BigInteger HALF_TICK = BigInteger.valueOf(BASIS / 2);

    private MarketMath() {}

    static long roundRatio(long numerator, long denominator) {
        if (denominator <= 0) {
            throw new IllegalArgumentException("Rounding denominator must be positive");
        }
        long quotient = numerator / denominator;
        long remainder = numerator % denominator;
        long threshold = denominator - denominator / 2;
        return Math.abs(remainder) < threshold ? quotient : quotient + Long.signum(remainder);
    }

    static int scale(int bps, int perMille) {
        return Math.toIntExact(roundRatio((long) bps * perMille, 1000));
    }

    static int clamp(long value, int lower, int upper) {
        return (int) Math.max(lower, Math.min(upper, value));
    }

    static void requireRange(long value, long lower, long upper, String field) {
        if (value < lower || value > upper) {
            throw new IllegalArgumentException(field + " is outside the permitted range");
        }
    }

    static long advanceIndex(long previous, int bps) {
        requireRange(previous, MIN_INDEX, MAX_INDEX, "Previous index");
        requireRange(bps, -BASIS + 1, BASIS, "Return");
        BigInteger candidate =
                BigInteger.valueOf(previous)
                        .multiply(BigInteger.valueOf(BASIS + bps))
                        .add(HALF_TICK)
                        .divide(INDEX_DIVISOR);
        if (candidate.compareTo(BigInteger.valueOf(MIN_INDEX)) < 0
                || candidate.compareTo(BigInteger.valueOf(MAX_INDEX)) > 0) {
            throw new ArithmeticException("Market index exceeded its numerical limits");
        }
        return candidate.longValueExact();
    }
}
