package game.bullbear.application.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import game.bullbear.domain.charm.CharmCategory;
import game.bullbear.domain.news.NewsSourceId;
import game.bullbear.domain.stock.StockId;
import java.util.List;
import org.junit.jupiter.api.Test;

class GameDesignCatalogTest {
    private final GameDesignCatalog catalog = new GameDesignCatalog();

    @Test
    void exposesEveryStockIdentity() {
        assertEquals(List.of(StockId.values()), catalog.stockIds());
    }

    @Test
    void exposesEverySourceWithoutAVisibilityFilter() {
        assertEquals(List.of(NewsSourceId.values()), catalog.sourceIds());
    }

    @Test
    void exposesEveryCharmCategory() {
        assertEquals(List.of(CharmCategory.values()), catalog.charmCategories());
    }

    @Test
    void exposedListsAreUnmodifiable() {
        assertThrows(UnsupportedOperationException.class, () -> catalog.stockIds().clear());
        assertThrows(UnsupportedOperationException.class, () -> catalog.sourceIds().clear());
        assertThrows(UnsupportedOperationException.class, () -> catalog.charmCategories().clear());
    }

    @Test
    void separateInstancesAndRepeatedReadsExposeEqualMetadata() {
        var second = new GameDesignCatalog();
        assertEquals(catalog.stockIds(), second.stockIds());
        assertEquals(catalog.sourceIds(), second.sourceIds());
        assertEquals(catalog.charmCategories(), second.charmCategories());
        assertEquals(catalog.stockIds(), catalog.stockIds());
        assertEquals(catalog.sourceIds(), catalog.sourceIds());
        assertEquals(catalog.charmCategories(), catalog.charmCategories());
    }
}
