package game.bullbear.domain.game;

public record SpecialCurrencyAmount(long units) {
    public static final SpecialCurrencyAmount ZERO = new SpecialCurrencyAmount(0);

    public SpecialCurrencyAmount {
        if (units < 0) {
            throw new IllegalArgumentException("Special currency cannot be negative");
        }
    }

    public SpecialCurrencyAmount plus(SpecialCurrencyAmount other) {
        return new SpecialCurrencyAmount(Math.addExact(units, other.units));
    }
}
