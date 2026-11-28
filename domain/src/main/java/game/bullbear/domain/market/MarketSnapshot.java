package game.bullbear.domain.market;

import game.bullbear.domain.game.RoundKey;
import game.bullbear.domain.game.RunId;
import game.bullbear.domain.stock.StockId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;

/** Accepted market data suitable for projection outside the trusted market use case. */
public record MarketSnapshot(
        RunId runId,
        long sequence,
        MarketRegime regime,
        Map<StockId, Quote> quotes,
        List<HistoryRound> history) {
    public record Quote(long indexTicks, OptionalInt lastReturnBps) {
        public Quote {
            Objects.requireNonNull(lastReturnBps);
        }
    }

    public record PriceMove(long beforeTicks, long afterTicks, int returnBps) {}

    public record HistoryRound(RoundKey key, MarketRegime regime, Map<StockId, PriceMove> moves) {
        public HistoryRound {
            Objects.requireNonNull(key);
            Objects.requireNonNull(regime);
            moves = MarketRules.orderedMap(MarketRules.STOCKS, moves);
        }
    }

    public MarketSnapshot {
        Objects.requireNonNull(runId);
        Objects.requireNonNull(regime);
        quotes = MarketRules.orderedMap(MarketRules.STOCKS, quotes);
        history = List.copyOf(history);
    }
}
