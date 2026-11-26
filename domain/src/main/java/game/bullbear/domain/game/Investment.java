package game.bullbear.domain.game;

import game.bullbear.domain.stock.StockId;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Data accepted at the commitment and settlement boundaries. */
public final class Investment {
    private Investment() {}

    public record Allocation(Map<StockId, CoinAmount> amounts) {
        public Allocation {
            var positive = new EnumMap<StockId, CoinAmount>(StockId.class);
            var supplied = stockMap(amounts);
            for (var entry : supplied.entrySet()) {
                if (entry.getValue().units() > 0) {
                    positive.put(entry.getKey(), entry.getValue());
                }
            }
            totalOf(positive);
            amounts = Collections.unmodifiableMap(positive);
        }

        public CoinAmount total() {
            return totalOf(amounts);
        }
    }

    public record Commitment(RoundKey key, Allocation allocation) {
        public Commitment {
            Objects.requireNonNull(key);
            if (allocation.amounts().isEmpty()) {
                throw new IllegalArgumentException("Investment must have a positive stake");
            }
        }
    }

    public enum Movement {
        UP,
        DOWN
    }

    public record Settlement(
            Commitment commitment,
            Map<StockId, Movement> movements,
            Map<StockId, CoinAmount> returned,
            SpecialCurrencyAmount reward,
            Optional<Shop.Rotation> rotation) {
        public Settlement {
            movements = Collections.unmodifiableMap(stockMap(movements));
            returned = Collections.unmodifiableMap(stockMap(returned));
            Objects.requireNonNull(rotation);
            if (movements.size() != StockId.values().length) {
                throw new IllegalArgumentException("One movement per stock is required");
            }
            if (!returned.keySet().equals(commitment.allocation().amounts().keySet())) {
                throw new IllegalArgumentException(
                        "Returns must cover exactly the invested stocks");
            }
            long change =
                    Math.subtractExact(
                            totalOf(returned).units(), commitment.allocation().total().units());
            if (reward.units() > 0 && change <= 0) {
                throw new IllegalArgumentException("A reward requires a profit");
            }
        }

        public CoinAmount totalReturned() {
            return totalOf(returned);
        }

        public long profitOrLoss() {
            return Math.subtractExact(
                    totalReturned().units(), commitment.allocation().total().units());
        }
    }

    private static <T> EnumMap<StockId, T> stockMap(Map<StockId, T> values) {
        var copy = new EnumMap<StockId, T>(StockId.class);
        values.forEach((stock, value) -> copy.put(stock, Objects.requireNonNull(value)));
        return copy;
    }

    private static CoinAmount totalOf(Map<StockId, CoinAmount> amounts) {
        var total = CoinAmount.ZERO;
        for (var amount : amounts.values()) {
            total = total.plus(amount);
        }
        return total;
    }
}
