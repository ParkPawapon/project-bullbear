package game.bullbear.domain.game;

import static game.bullbear.domain.game.GameFixtures.*;
import static org.junit.jupiter.api.Assertions.*;

import game.bullbear.domain.charm.CharmCategory;
import game.bullbear.domain.news.NewsSourceId;
import game.bullbear.domain.stock.StockId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @Test
    void allocationIsCopiedNormalizedAndCheckedAtConstruction() {
        var input =
                new HashMap<>(
                        Map.of(
                                StockId.STABLE,
                                coin(4),
                                StockId.GROWTH,
                                coin(0),
                                StockId.VOLATILE,
                                coin(2)));
        var allocation = new Investment.Allocation(input);
        input.clear();
        assertEquals(
                List.of(StockId.STABLE, StockId.VOLATILE),
                List.copyOf(allocation.amounts().keySet()));
        assertEquals(coin(6), allocation.total());
        assertThrows(UnsupportedOperationException.class, () -> allocation.amounts().clear());
        assertThrows(
                ArithmeticException.class,
                () ->
                        new Investment.Allocation(
                                Map.of(
                                        StockId.STABLE,
                                        coin(Long.MAX_VALUE),
                                        StockId.GROWTH,
                                        coin(1))));
        input.put(StockId.STABLE, null);
        assertThrows(NullPointerException.class, () -> new Investment.Allocation(input));
        assertEquals(coin(0), new Investment.Allocation(Map.of()).total());
    }

    @Test
    void settlementValidatesAllMovementsReturnCoverageAndReward() {
        var commitment =
                new Investment.Commitment(
                        new RoundKey(new RunId(1), 1, 1),
                        new Investment.Allocation(
                                Map.of(StockId.STABLE, coin(2), StockId.GROWTH, coin(4))));
        var directions = movements();
        var returns = new HashMap<>(Map.of(StockId.STABLE, coin(0), StockId.GROWTH, coin(8)));
        var settlement =
                new Investment.Settlement(
                        commitment, directions, returns, special(2), Optional.empty());
        assertEquals(coin(8), settlement.totalReturned());
        assertEquals(2L, settlement.profitOrLoss());
        assertThrows(UnsupportedOperationException.class, () -> settlement.returned().clear());
        assertThrows(UnsupportedOperationException.class, () -> settlement.movements().clear());
        returns.put(StockId.GROWTH, coin(6));
        assertEquals(
                0L,
                new Investment.Settlement(
                                commitment, directions, returns, special(0), Optional.empty())
                        .profitOrLoss());
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new Investment.Settlement(
                                commitment, directions, returns, special(1), Optional.empty()));
        returns.put(StockId.GROWTH, coin(5));
        assertEquals(
                -1L,
                new Investment.Settlement(
                                commitment, directions, returns, special(0), Optional.empty())
                        .profitOrLoss());
        returns.put(StockId.GROWTH, coin(Long.MAX_VALUE));
        returns.put(StockId.STABLE, coin(1));
        assertThrows(
                ArithmeticException.class,
                () ->
                        new Investment.Settlement(
                                commitment, directions, returns, special(0), Optional.empty()));
        returns.remove(StockId.STABLE);
        returns.put(StockId.VOLATILE, coin(0));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new Investment.Settlement(
                                commitment, directions, returns, special(0), Optional.empty()));
        directions.remove(StockId.WILDCARD);
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new Investment.Settlement(
                                commitment,
                                directions,
                                settlement.returned(),
                                special(0),
                                Optional.empty()));
        assertEquals(CoinAmount.ZERO, settlement.returned().get(StockId.STABLE));
        assertEquals(5, settlement.movements().size());
    }

    @Test
    void newsIdentityIsUniqueWithinAKeyedBatchAndBatchTextIsRedacted() {
        var key = new RoundKey(new RunId(1), 1, 1);
        var claim =
                new NewsClaim(
                        new NewsClaim.Id("hidden-fixture"),
                        NewsSourceId.D,
                        StockId.FOLLOWER,
                        NewsClaim.Polarity.NEGATIVE);
        var input = new ArrayList<>(List.of(claim));
        var round = new RoundEntry(key, coin(0), coin(0), input);
        input.clear();
        assertEquals(List.of(claim), round.claims());
        assertFalse(round.toString().contains("hidden-fixture"));
        assertThrows(UnsupportedOperationException.class, () -> round.claims().clear());
        assertThrows(IllegalArgumentException.class, () -> entry(key, 0, 0, claim, claim));
        assertThrows(IllegalArgumentException.class, () -> new NewsClaim.Id(""));
        assertThrows(
                NullPointerException.class,
                () ->
                        new NewsClaim(
                                claim.id(), null, StockId.FOLLOWER, NewsClaim.Polarity.NEGATIVE));
    }

    @Test
    void shopIsFixedOrderedAndCheckedBeforeRotation() {
        var offer = Optional.of(CHARM);
        var empty = Optional.<CharmDefinition.Id>empty();
        var slots = new ArrayList<>(List.of(offer, empty));
        var shop = new Shop(3, slots);
        slots.clear();
        assertEquals(List.of(offer, empty), shop.slots());
        assertEquals(List.of(empty, empty), shop.consume(3, new Shop.SlotId(1), CHARM).slots());
        assertThrows(GameRejection.class, () -> shop.consume(2, new Shop.SlotId(1), CHARM));
        assertThrows(
                GameRejection.class,
                () -> shop.consume(3, new Shop.SlotId(1), new CharmDefinition.Id("wrong")));
        assertThrows(GameRejection.class, () -> shop.consume(3, new Shop.SlotId(2), CHARM));
        assertThrows(IllegalArgumentException.class, () -> new Shop.SlotId(0));
        assertThrows(
                IndexOutOfBoundsException.class, () -> shop.consume(3, new Shop.SlotId(3), CHARM));
        assertThrows(UnsupportedOperationException.class, () -> shop.slots().clear());
        assertThrows(IllegalArgumentException.class, () -> new Shop(-1, List.of(empty)));
        assertThrows(IllegalArgumentException.class, () -> new Shop.Rotation(-1, List.of(empty)));
        assertThrows(
                IllegalArgumentException.class,
                () -> shop.rotate(new Shop.Rotation(3, List.of(empty)), catalog()));
        assertThrows(
                ArithmeticException.class,
                () ->
                        new Shop(Long.MAX_VALUE, List.of(offer, empty))
                                .rotate(
                                        new Shop.Rotation(Long.MAX_VALUE, List.of(empty, offer)),
                                        catalog()));
    }
}
