package game.bullbear.domain;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import game.bullbear.domain.charm.CharmCategory;
import game.bullbear.domain.news.NewsSourceId;
import game.bullbear.domain.stock.StockId;
import org.junit.jupiter.api.Test;

class DesignIdentifierTest {
    @Test
    void stockIdentitiesMatchTheDesignBaseline() {
        assertArrayEquals(
                new String[] {"STABLE", "GROWTH", "VOLATILE", "FOLLOWER", "WILDCARD"},
                names(StockId.values()));
    }

    @Test
    void newsSourcesIncludeAllFiveIdentities() {
        assertArrayEquals(new String[] {"A", "B", "C", "D", "E"}, names(NewsSourceId.values()));
    }

    @Test
    void charmCategoriesMatchTheDesignTaxonomy() {
        assertArrayEquals(
                new String[] {"INFORMATION", "SOURCE_SPECIFIC", "CONDITIONAL_MULTIPLIER"},
                names(CharmCategory.values()));
    }

    private static String[] names(Enum<?>[] values) {
        return java.util.Arrays.stream(values).map(Enum::name).toArray(String[]::new);
    }
}
