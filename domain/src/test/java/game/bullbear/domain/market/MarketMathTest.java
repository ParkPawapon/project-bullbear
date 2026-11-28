package game.bullbear.domain.market;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

final class MarketMathTest {
    @Test
    void scalingRoundsSignedValuesAndTiesSymmetrically() {
        assertEquals(550, MarketMath.scale(1000, 550));
        assertEquals(-550, MarketMath.scale(-1000, 550));
        assertEquals(1, MarketMath.scale(1, 500));
        assertEquals(-1, MarketMath.scale(-1, 500));
        assertEquals(1, MarketMath.scale(3, 400));
        assertEquals(-1, MarketMath.scale(-3, 400));
        assertEquals(0, MarketMath.scale(0, 1750));
        assertEquals(Long.MIN_VALUE, MarketMath.roundRatio(Long.MIN_VALUE, 1));
        assertEquals(-1, MarketMath.roundRatio(Long.MIN_VALUE, Long.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> MarketMath.roundRatio(1, 0));
        assertThrows(
                ArithmeticException.class,
                () -> MarketMath.scale(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    @Test
    void normalizedIndexUsesExactHalfTickRoundingAndRejectsLimits() {
        assertEquals(15_002, MarketMath.advanceIndex(10_001, 5000));
        assertEquals(10_000, MarketMath.advanceIndex(20_004, -5001));
        assertEquals(10_001, MarketMath.advanceIndex(20_001, -5000));
        assertEquals(1_088_200_000_000L, MarketMath.advanceIndex(1_000_000_000_000L, 882));
        assertEquals(MarketMath.MAX_INDEX, MarketMath.advanceIndex(MarketMath.MAX_INDEX, 0));
        assertEquals(MarketMath.MIN_INDEX, MarketMath.advanceIndex(MarketMath.MIN_INDEX, 0));
        assertThrows(
                ArithmeticException.class, () -> MarketMath.advanceIndex(MarketMath.MAX_INDEX, 1));
        assertThrows(
                ArithmeticException.class, () -> MarketMath.advanceIndex(MarketMath.MIN_INDEX, -1));
        assertThrows(IllegalArgumentException.class, () -> MarketMath.advanceIndex(9_999, 0));
        assertThrows(
                IllegalArgumentException.class, () -> MarketMath.advanceIndex(10_000, -10_000));
    }
}
