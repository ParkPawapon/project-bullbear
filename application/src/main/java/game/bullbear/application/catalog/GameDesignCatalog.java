package game.bullbear.application.catalog;

import game.bullbear.domain.charm.CharmCategory;
import game.bullbear.domain.news.NewsSourceId;
import game.bullbear.domain.stock.StockId;
import java.util.List;

public final class GameDesignCatalog {
    private final List<StockId> stockIds = List.of(StockId.values());
    private final List<NewsSourceId> sourceIds = List.of(NewsSourceId.values());
    private final List<CharmCategory> charmCategories = List.of(CharmCategory.values());

    public List<StockId> stockIds() {
        return stockIds;
    }

    public List<NewsSourceId> sourceIds() {
        return sourceIds;
    }

    public List<CharmCategory> charmCategories() {
        return charmCategories;
    }
}
