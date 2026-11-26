package game.bullbear.domain.game;

import game.bullbear.domain.news.NewsSourceId;
import game.bullbear.domain.stock.StockId;
import java.util.Objects;

public record NewsClaim(Id id, NewsSourceId source, StockId stock, Polarity polarity) {
    public NewsClaim {
        Objects.requireNonNull(id);
        Objects.requireNonNull(source);
        Objects.requireNonNull(stock);
        Objects.requireNonNull(polarity);
    }

    public record Id(String value) {
        public Id {
            if (value.isBlank()) {
                throw new IllegalArgumentException("News claim ID is required");
            }
        }
    }

    public enum Polarity {
        POSITIVE,
        NEGATIVE
    }
}
