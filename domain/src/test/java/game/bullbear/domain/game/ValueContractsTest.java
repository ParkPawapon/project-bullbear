package game.bullbear.domain.game;

import static game.bullbear.domain.game.GameFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import game.bullbear.domain.charm.CharmCategory;
import game.bullbear.domain.news.NewsSourceId;
import game.bullbear.domain.stock.StockId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ValueContractsTest {
    @Test
    void currenciesValidateAndCheckTheirOwnArithmetic() {
        assertEquals(CoinAmount.ZERO, coin(0));
        assertEquals(SpecialCurrencyAmount.ZERO, special(0));
        assertEquals(coin(10), coin(8).plus(coin(2)));
        assertEquals(coin(6), coin(8).minus(coin(2)));
        assertEquals(special(10), special(8).plus(special(2)));
        assertThrows(IllegalArgumentException.class, () -> coin(-1));
        assertThrows(IllegalArgumentException.class, () -> special(-1));
        assertThrows(IllegalArgumentException.class, () -> coin(0).minus(coin(1)));
        assertThrows(ArithmeticException.class, () -> coin(Long.MAX_VALUE).plus(coin(1)));
        assertThrows(ArithmeticException.class, () -> special(Long.MAX_VALUE).plus(special(1)));
        assertNotEquals((Object) coin(5), special(5));
    }

    @Test
    void roundIdentityIsScopedToRunStageAndRound() {
        var id = new RunId(7);
        var start = new RoundKey(id, 1, 1);
        assertNotEquals(start, new RoundKey(new RunId(8), 1, 1));
        assertNotEquals(start, start.nextRound());
        assertEquals(new RoundKey(id, 2, 1), start.nextStage());
        assertFalse(new RoundKey(id, 7, 3).endless());
        assertTrue(new RoundKey(id, 8, 1).endless());
        assertThrows(IllegalArgumentException.class, () -> new RunId(0));
        assertThrows(IllegalArgumentException.class, () -> new RoundKey(id, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new RoundKey(id, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new RoundKey(id, 1, 4));
        assertThrows(NullPointerException.class, () -> new RoundKey(null, 1, 1));
        var maximum = new RoundKey(id, Integer.MAX_VALUE, 3);
        assertThrows(ArithmeticException.class, maximum::nextStage);
    }

    @Test
    void explicitCatalogRetainsCanonicalStocksAndRejectsIncompleteDefinitions() {
        var sourceInput = new ArrayList<>(sources());
        var charmInput = new ArrayList<>(List.of(DEFINITION));
        var catalog = new GameCatalog(sourceInput.reversed(), charmInput);
        sourceInput.clear();
        charmInput.clear();
        assertEquals(List.of(StockId.values()), catalog.stocks());
        assertEquals(List.of(NewsSourceId.values()), List.copyOf(catalog.sources().keySet()));
        assertEquals(DEFINITION, catalog.charms().get(CHARM));
        assertThrows(UnsupportedOperationException.class, () -> catalog.sources().clear());
        assertThrows(UnsupportedOperationException.class, () -> catalog.charms().clear());
        assertThrows(
                IllegalArgumentException.class,
                () -> new GameCatalog(sources().subList(0, 4), List.of()));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new GameCatalog(
                                Arrays.asList(sources().getFirst(), sources().getFirst()),
                                List.of()));
        assertThrows(
                IllegalArgumentException.class,
                () -> new GameCatalog(sources(), List.of(DEFINITION, DEFINITION)));
    }

    @Test
    void definitionsValidateCurrencyAndBasisPointUnits() {
        assertEquals(
                0, new GameCatalog.SourceDefinition(NewsSourceId.A, 0, 0).credibilityBasisPoints());
        assertEquals(
                Long.MAX_VALUE,
                new GameCatalog.SourceDefinition(NewsSourceId.A, 10_000, Long.MAX_VALUE)
                        .impactBasisPoints());
        assertThrows(
                IllegalArgumentException.class,
                () -> new GameCatalog.SourceDefinition(NewsSourceId.A, -1, 0));
        assertThrows(
                IllegalArgumentException.class,
                () -> new GameCatalog.SourceDefinition(NewsSourceId.A, 10_001, 0));
        assertThrows(
                IllegalArgumentException.class,
                () -> new GameCatalog.SourceDefinition(NewsSourceId.A, 0, -1));
        assertThrows(
                NullPointerException.class, () -> new GameCatalog.SourceDefinition(null, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new CharmDefinition.Id(" "));
        assertThrows(
                IllegalArgumentException.class,
                () -> new CharmDefinition(CHARM, CharmCategory.INFORMATION, special(0)));
        assertThrows(
                NullPointerException.class, () -> new CharmDefinition(CHARM, null, special(1)));
        assertThrows(IllegalArgumentException.class, () -> owned(0));
    }

    @Test
    void rulesRequireIncreasingGoalsAndBoundedExplicitInputs() {
        var goals = new ArrayList<>(goals());
        var visibility = EnumSet.copyOf(SOURCES);
        var rules = new GameRules(goals, coin(0), special(0), visibility, 1, 1024);
        goals.set(0, coin(0));
        visibility.clear();
        assertEquals(coin(1), rules.campaignGoals().getFirst());
        assertEquals(SOURCES, rules.initialSources());
        assertThrows(UnsupportedOperationException.class, () -> rules.campaignGoals().clear());
        assertThrows(UnsupportedOperationException.class, () -> rules.initialSources().clear());
        assertThrows(
                IllegalArgumentException.class,
                () -> new GameRules(goals, coin(0), special(0), SOURCES, 1, 1));
        goals.set(0, coin(2));
        assertThrows(
                IllegalArgumentException.class,
                () -> new GameRules(goals, coin(0), special(0), SOURCES, 1, 1));
        goals.set(0, coin(3));
        assertThrows(
                IllegalArgumentException.class,
                () -> new GameRules(goals, coin(0), special(0), SOURCES, 1, 1));
        assertThrows(
                IllegalArgumentException.class,
                () -> new GameRules(List.of(coin(1)), coin(0), special(0), SOURCES, 1, 1));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new GameRules(
                                goals(),
                                coin(0),
                                special(0),
                                Set.of(NewsSourceId.A, NewsSourceId.B),
                                1,
                                1));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new GameRules(
                                goals(),
                                coin(0),
                                special(0),
                                EnumSet.allOf(NewsSourceId.class),
                                1,
                                1));
        assertThrows(
                IllegalArgumentException.class,
                () -> new GameRules(goals(), coin(0), special(0), SOURCES, 0, 1));
        assertThrows(
                IllegalArgumentException.class,
                () -> new GameRules(goals(), coin(0), special(0), SOURCES, 1, 1025));
        assertThrows(
                NullPointerException.class,
                () -> new GameRules(goals(), null, special(0), SOURCES, 1, 1));
        assertThrows(
                NullPointerException.class,
                () -> new GameRules(goals(), coin(0), null, SOURCES, 1, 1));
    }
}
