package game.bullbear.domain.market;

import game.bullbear.domain.game.RoundKey;
import game.bullbear.domain.stock.StockId;
import java.util.Objects;
import java.util.Optional;

/** Already resolved signed influence. Truth and visibility are owned upstream. */
public record MarketImpact(
        String id, StockId target, int bps, Channel channel, Optional<EventRef> cause) {
    public enum Channel {
        SENTIMENT,
        REPORT_ONLY,
        FUNDAMENTAL
    }

    public record EventRef(RoundKey round, String definitionId) {
        public EventRef {
            Objects.requireNonNull(round);
            MarketRules.requireIdentifier(definitionId);
        }
    }

    public MarketImpact {
        MarketRules.requireIdentifier(id);
        Objects.requireNonNull(target);
        Objects.requireNonNull(channel);
        Objects.requireNonNull(cause);
        MarketMath.requireRange(bps, -3000, 3000, "Impact");
        if (channel != Channel.SENTIMENT && (bps != 0 || cause.isEmpty())) {
            throw new IllegalArgumentException(
                    "An event reference may report its existing shock with zero impact");
        }
    }
}
