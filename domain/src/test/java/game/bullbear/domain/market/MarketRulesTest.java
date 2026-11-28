package game.bullbear.domain.market;

import static game.bullbear.domain.market.MarketRegime.*;
import static org.junit.jupiter.api.Assertions.*;

import game.bullbear.domain.stock.StockId;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class MarketRulesTest {
    private static final MarketRules DEFAULT = MarketTuning.provisional();

    @Test
    void defaultCatalogCoversEveryStockWithFavorableAndAdverseEvents() {
        assertEquals(EnumSet.allOf(StockId.class), DEFAULT.profiles().keySet());
        assertEquals(MarketRules.STOCKS, List.copyOf(DEFAULT.profiles().keySet()));
        assertEquals(MarketRules.REGIMES, List.copyOf(DEFAULT.regimes().keySet()));
        assertEquals(16, DEFAULT.eventWeight());
        assertEquals("phase03-v1", DEFAULT.version());
        for (StockId stock : MarketRules.STOCKS) {
            assertTrue(
                    DEFAULT.events().stream()
                            .anyMatch(e -> e.targets().contains(stock) && e.shockBps() > 0));
            assertTrue(
                    DEFAULT.events().stream()
                            .anyMatch(e -> e.targets().contains(stock) && e.shockBps() < 0));
        }
    }

    @Test
    void invalidProfilesAndSettingsAreRejectedAtConstruction() {
        assertThrows(IllegalArgumentException.class, () -> new StockProfile(-1, 0, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new StockProfile(5001, 0, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new StockProfile(0, 2001, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new StockProfile(0, 0, -1, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new StockProfile(0, 0, 0, 2001, 0));
        assertThrows(IllegalArgumentException.class, () -> new StockProfile(0, 0, 0, 0, 501));
        assertThrows(IllegalArgumentException.class, () -> settings(0, -5000, 6000, 64));
        assertThrows(IllegalArgumentException.class, () -> settings(10_000, -10000, 6000, 64));
        assertThrows(IllegalArgumentException.class, () -> settings(10_000, -5000, 0, 64));
        assertThrows(IllegalArgumentException.class, () -> settings(10_000, -5000, 6000, 0));
        assertThrows(IllegalArgumentException.class, () -> settings(10_000, -5000, 6000, 513));

        Map<StockId, StockProfile> incomplete = new HashMap<>(DEFAULT.profiles());
        incomplete.remove(StockId.STABLE);
        assertThrows(IllegalArgumentException.class, () -> rules(incomplete, DEFAULT.events()));
        incomplete.put(null, new StockProfile(0, 0, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> rules(incomplete, DEFAULT.events()));
        incomplete.remove(null);
        incomplete.put(StockId.STABLE, null);
        assertThrows(NullPointerException.class, () -> rules(incomplete, DEFAULT.events()));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        Map.of(
                                StockId.STABLE,
                                DEFAULT.profiles().get(StockId.STABLE),
                                StockId.STABLE,
                                DEFAULT.profiles().get(StockId.GROWTH)));
    }

    @Test
    void transitionTableRejectsMissingDuplicateOutOfRangeAndIncorrectTotals() {
        Map<MarketRegime, Integer> weights =
                new EnumMap<>(DEFAULT.regimes().get(BULL).transitions());
        weights.put(BULL, 5399);
        assertThrows(IllegalArgumentException.class, () -> regime(weights));
        weights.put(BULL, 5401);
        assertThrows(IllegalArgumentException.class, () -> regime(weights));
        weights.put(BULL, -1);
        assertThrows(IllegalArgumentException.class, () -> regime(weights));
        weights.remove(BULL);
        assertThrows(IllegalArgumentException.class, () -> regime(weights));
        List<MarketRules.Regime> rows = new ArrayList<>(DEFAULT.regimes().values());
        rows.removeLast();
        assertThrows(
                IllegalArgumentException.class,
                () -> rules(DEFAULT.version(), rows, DEFAULT.events()));
        rows.add(rows.getFirst());
        assertThrows(
                IllegalArgumentException.class,
                () -> rules(DEFAULT.version(), rows, DEFAULT.events()));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new MarketRules.Regime(
                                BULL, 0, 0, 0, DEFAULT.regimes().get(BULL).transitions()));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new MarketRules.Regime(
                                BULL, 0, 1000, 10001, DEFAULT.regimes().get(BULL).transitions()));
    }

    @Test
    void invalidEventCatalogAndIdentityAreRejected() {
        var category = MarketRules.EventCategory.SECTOR;
        var targets = Set.of(StockId.GROWTH);
        assertThrows(
                IllegalArgumentException.class,
                () -> new MarketRules.Event("A|B", category, targets, 10, 1));
        assertThrows(
                IllegalArgumentException.class,
                () -> new MarketRules.Event("A", category, Set.of(), 10, 1));
        assertThrows(
                IllegalArgumentException.class,
                () -> new MarketRules.Event("A", category, targets, 3001, 1));
        assertThrows(
                IllegalArgumentException.class,
                () -> new MarketRules.Event("A", category, targets, 10, 0));
        var event = new MarketRules.Event("A", category, targets, 10, 1);
        assertThrows(
                IllegalArgumentException.class,
                () -> rules(DEFAULT.profiles(), List.of(event, event)));
        assertThrows(IllegalArgumentException.class, () -> rules(DEFAULT.profiles(), List.of()));
        assertThrows(
                IllegalArgumentException.class,
                () -> rules(DEFAULT.profiles(), java.util.Collections.nCopies(65, event)));
        assertThrows(
                IllegalArgumentException.class,
                () -> rules(" ", List.copyOf(DEFAULT.regimes().values()), DEFAULT.events()));
    }

    @Test
    void canonicalIdentityIsIndependentOfInputOrderAndObjectsAreDefensivelyCopied() {
        Map<StockId, StockProfile> profiles = new LinkedHashMap<>();
        for (StockId id : MarketRules.STOCKS.reversed()) {
            profiles.put(id, DEFAULT.profiles().get(id));
        }
        List<MarketRules.Regime> rows =
                new ArrayList<>(List.copyOf(DEFAULT.regimes().values()).reversed());
        List<MarketRules.Event> events = new ArrayList<>(DEFAULT.events().reversed());
        var reordered =
                new MarketRules(DEFAULT.version(), DEFAULT.settings(), profiles, rows, events);
        profiles.clear();
        rows.clear();
        events.clear();
        assertEquals(DEFAULT.fingerprint(), reordered.fingerprint());
        assertEquals(DEFAULT.events(), reordered.events());
        assertEquals(MarketRules.STOCKS, List.copyOf(reordered.profiles().keySet()));
        assertEquals(MarketRules.REGIMES, List.copyOf(reordered.regimes().keySet()));
        assertThrows(UnsupportedOperationException.class, () -> reordered.profiles().clear());
        assertThrows(UnsupportedOperationException.class, () -> reordered.events().clear());

        var weights = new HashMap<>(DEFAULT.regimes().get(BULL).transitions());
        var regenerated = regime(weights);
        weights.clear();
        assertEquals(5400, regenerated.transitions().get(BULL));
        var targets = EnumSet.of(StockId.STABLE);
        var event = new MarketRules.Event("A", MarketRules.EventCategory.SECTOR, targets, 10, 1);
        targets.clear();
        assertEquals(Set.of(StockId.STABLE), event.targets());
        assertThrows(UnsupportedOperationException.class, () -> event.targets().clear());
    }

    @Test
    void versionOrContentChangesRulesIdentity() {
        var rows = List.copyOf(DEFAULT.regimes().values());
        var differentVersion = rules("version2", rows, DEFAULT.events());
        assertNotEquals(DEFAULT.fingerprint(), differentVersion.fingerprint());
        var events = new ArrayList<>(DEFAULT.events());
        var original = events.removeFirst();
        events.add(
                new MarketRules.Event(
                        original.id(),
                        original.category(),
                        original.targets(),
                        original.shockBps() + 1,
                        original.weight()));
        assertNotEquals(DEFAULT.fingerprint(), rules(DEFAULT.profiles(), events).fingerprint());
    }

    private static MarketRules.Settings settings(long index, int lower, int upper, int capacity) {
        return new MarketRules.Settings(index, SIDEWAYS, 0, 0, 0, 0, lower, upper, capacity);
    }

    private static MarketRules.Regime regime(Map<MarketRegime, Integer> weights) {
        return new MarketRules.Regime(BULL, 320, 1000, 1800, weights);
    }

    private static MarketRules rules(
            Map<StockId, StockProfile> profiles, List<MarketRules.Event> events) {
        return new MarketRules(
                DEFAULT.version(),
                DEFAULT.settings(),
                profiles,
                List.copyOf(DEFAULT.regimes().values()),
                events);
    }

    private static MarketRules rules(
            String version, List<MarketRules.Regime> rows, List<MarketRules.Event> events) {
        return new MarketRules(version, DEFAULT.settings(), DEFAULT.profiles(), rows, events);
    }
}
