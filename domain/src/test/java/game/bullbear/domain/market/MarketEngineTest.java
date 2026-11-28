package game.bullbear.domain.market;

import static game.bullbear.domain.market.MarketDraws.Channel.*;
import static game.bullbear.domain.market.MarketRegime.*;
import static game.bullbear.domain.stock.StockId.*;
import static org.junit.jupiter.api.Assertions.*;

import game.bullbear.domain.game.RoundKey;
import game.bullbear.domain.game.RunId;
import game.bullbear.domain.market.MarketEngine.Attribution;
import game.bullbear.domain.market.MarketImpact.Channel;
import game.bullbear.domain.market.MarketImpact.EventRef;
import game.bullbear.domain.stock.StockId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class MarketEngineTest {
    private static final long OPEN = 1_000_000_000_000L;
    private static final RunId RUN = new RunId(1);
    private static final MarketDraws CENTRE = (round, channel, subject, index, bound) -> bound / 2;
    private static final StockProfile ZERO = new StockProfile(0, 0, 0, 0, 0);
    private static final MarketRules DEFAULT = MarketTuning.provisional();

    @Test
    void openingProjectionContainsAllStocksAndNoResultOrLatentEvent() {
        var engine = new MarketEngine(DEFAULT);
        var initial = engine.initial(RUN);
        var view = initial.snapshot();
        assertEquals(0, view.sequence());
        assertEquals(SIDEWAYS, view.regime());
        assertEquals(MarketRules.STOCKS, List.copyOf(view.quotes().keySet()));
        assertTrue(view.history().isEmpty());
        view.quotes()
                .values()
                .forEach(
                        q -> {
                            assertEquals(OPEN, q.indexTicks());
                            assertTrue(q.lastReturnBps().isEmpty());
                        });

        var hidden = engine.prepare(initial, key(1), (round, channel, subject, index, bound) -> 0);
        var noEvent = engine.prepare(initial, key(1), CENTRE);
        assertTrue(hidden.event().isPresent());
        assertTrue(noEvent.event().isEmpty());
        assertEquals(noEvent.planning(), hidden.planning());
        engine.resolve(hidden, List.of());
        assertSame(view, initial.snapshot());
        assertThrows(UnsupportedOperationException.class, () -> view.quotes().clear());
        assertThrows(
                IllegalArgumentException.class,
                () -> new MarketSnapshot(RUN, 0, SIDEWAYS, Map.of(), List.of()));
    }

    @Test
    void signedFormulaMatchesWorkedOracleAndUsesAcceptedReturnForMomentum() {
        var rules =
                rules(
                        BULL,
                        OPEN,
                        700,
                        350,
                        1800,
                        1800,
                        64,
                        DEFAULT.profiles(),
                        320,
                        1000,
                        5000,
                        List.of(event("TARGET", -400, GROWTH)));
        var engine = new MarketEngine(rules);
        var initial = engine.initial(RUN);
        var first =
                engine.resolve(
                        engine.prepare(initial, key(1), CENTRE),
                        List.of(impact("first", GROWTH, 648)));
        assertEquals(0, first.attribution().get(GROWTH).momentumBps());
        assertEquals(1000, first.proposedRound().moves().get(GROWTH).returnBps());
        var accepted = engine.accept(initial, first);
        MarketDraws controlled =
                (round, channel, subject, index, bound) ->
                        switch (channel) {
                            case COMMON_NOISE -> bound / 2 - 200;
                            case STOCK_NOISE ->
                                    subject.equals(GROWTH.name()) ? bound / 2 + 800 : bound / 2;
                            default -> 0;
                        };
        var prepared = engine.prepare(accepted, key(2), controlled);
        var proposal = engine.resolve(prepared, List.of(impact("sentiment", GROWTH, 250)));
        assertEquals(
                new Attribution(352, -190, 800, 130, -460, 250, 882, 0, 882),
                proposal.attribution().get(GROWTH));
        assertEquals(
                new MarketSnapshot.PriceMove(1_100_000_000_000L, 1_197_020_000_000L, 882),
                proposal.proposedRound().moves().get(GROWTH));
        assertEquals(-110, proposal.attribution().get(STABLE).commonBps());
        assertEquals(-280, proposal.attribution().get(FOLLOWER).commonBps());
        assertEquals(0, proposal.attribution().get(STABLE).ownBps());
        assertEquals(0, proposal.attribution().get(STABLE).eventBps());
        assertEquals(prepared.planning().mood(), proposal.proposedRound().regime());
    }

    @Test
    void transitionBoundariesSkipZeroWeightAndOpeningDraw() {
        var row =
                Map.of(
                        BULL,
                        2000,
                        BEAR,
                        0,
                        SIDEWAYS,
                        3000,
                        HIGH_VOLATILITY,
                        5000,
                        CRASH,
                        0,
                        RECOVERY,
                        0);
        var regimes =
                MarketRules.REGIMES.stream()
                        .map(id -> new MarketRules.Regime(id, 0, 1000, 0, row))
                        .toList();
        var rules =
                new MarketRules(
                        "weight-fixture",
                        settings(OPEN, CRASH, 0, 0, 0, 0, 2),
                        uniform(ZERO),
                        regimes,
                        List.of());
        var engine = new MarketEngine(rules);
        var initial = engine.initial(RUN);
        var first =
                engine.prepare(
                        initial,
                        key(1),
                        (round, channel, subject, index, bound) -> {
                            assertEquals(EVENT_OCCURRENCE, channel);
                            return 0;
                        });
        assertEquals(CRASH, first.planning().mood());
        var state = engine.accept(initial, engine.resolve(first, List.of()));
        int[] draws = {0, 1999, 2000, 4999, 5000, 9999};
        MarketRegime[] expectedMoods = {
            BULL, BULL, SIDEWAYS, SIDEWAYS, HIGH_VOLATILITY, HIGH_VOLATILITY
        };
        for (int i = 0; i < draws.length; i++) {
            int value = draws[i];
            assertEquals(
                    expectedMoods[i],
                    engine.prepare(state, key(2), (r, c, s, d, b) -> value).planning().mood());
        }
        assertThrows(
                IllegalArgumentException.class,
                () -> engine.prepare(state, key(2), (r, c, s, i, b) -> b));
        assertThrows(
                IllegalArgumentException.class,
                () -> engine.prepare(state, key(2), (r, c, s, i, b) -> -1));
    }

    @Test
    void everyDefaultTransitionRowPersistsAndCrashCanRecover() {
        for (MarketRegime mood : MarketRules.REGIMES) {
            var settings = settings(OPEN, mood, 0, 0, 1800, 0, 2);
            var rules =
                    new MarketRules(
                            "transition-fixture",
                            settings,
                            uniform(ZERO),
                            List.copyOf(DEFAULT.regimes().values()),
                            DEFAULT.events());
            var engine = new MarketEngine(rules);
            var initial = engine.initial(RUN);
            var state =
                    engine.accept(
                            initial,
                            engine.resolve(engine.prepare(initial, key(1), CENTRE), List.of()));
            int boundary = 0;
            for (MarketRegime next : MarketRules.REGIMES) {
                int weight = rules.regimes().get(mood).transitions().get(next);
                if (weight > 0) {
                    int draw = boundary;
                    assertEquals(
                            next,
                            engine.prepare(
                                            state,
                                            key(2),
                                            (r, c, s, i, b) ->
                                                    c == REGIME_TRANSITION ? draw : b / 2)
                                    .planning()
                                    .mood());
                }
                boundary += weight;
            }
        }
    }

    @Test
    void moodAndVolatilityScaleTheSameDrawsAcrossAllProfiles() {
        var bull = moodProposal(BULL, 320, 1000, CENTRE);
        var bear = moodProposal(BEAR, -320, 1000, CENTRE);
        assertTrue(
                MarketRules.STOCKS.stream()
                        .allMatch(
                                s ->
                                        bull.attribution().get(s).moodBps()
                                                > bear.attribution().get(s).moodBps()));
        MarketDraws positive = (r, c, s, i, b) -> b - 1;
        var sideways = moodProposal(SIDEWAYS, 0, 900, positive);
        var highVolatility = moodProposal(HIGH_VOLATILITY, 0, 1550, positive);
        assertTrue(
                MarketRules.STOCKS.stream()
                        .allMatch(
                                s ->
                                        highVolatility.attribution().get(s).ownBps()
                                                > sideways.attribution().get(s).ownBps()));
    }

    @Test
    void eventChoiceIsFrozenCanonicalAndAppliesExactlyOncePerTarget() {
        var catalog =
                List.of(event("Z_ADVERSE", -1000, WILDCARD), event("A_BROAD", 900, STABLE, GROWTH));
        var rules =
                rules(
                        SIDEWAYS,
                        OPEN,
                        0,
                        0,
                        800,
                        1800,
                        2,
                        DEFAULT.profiles(),
                        0,
                        1000,
                        10000,
                        catalog);
        var engine = new MarketEngine(rules);
        var initial = engine.initial(RUN);
        var calls = new ArrayList<MarketDraws.Channel>();
        var first =
                engine.prepare(
                        initial,
                        key(1),
                        (r, c, s, i, b) -> {
                            calls.add(c);
                            return c == EVENT_SELECTION ? 0 : b - 1;
                        });
        assertEquals(
                List.of(EVENT_OCCURRENCE, EVENT_SELECTION),
                calls.stream().filter(c -> c == EVENT_OCCURRENCE || c == EVENT_SELECTION).toList());
        var ref = first.event().orElseThrow().reference();
        assertEquals(new EventRef(key(1), "A_BROAD"), ref);
        int drawsAtPrepare = calls.size();
        var reports =
                List.of(
                        new MarketImpact(
                                "report", STABLE, 0, Channel.REPORT_ONLY, Optional.of(ref)),
                        new MarketImpact(
                                "fundamental", GROWTH, 0, Channel.FUNDAMENTAL, Optional.of(ref)),
                        new MarketImpact(
                                "sentiment", GROWTH, 125, Channel.SENTIMENT, Optional.of(ref)));
        var broad = engine.resolve(first, reports);
        assertEquals(broad.attribution(), engine.resolve(first, reports.reversed()).attribution());
        assertEquals(drawsAtPrepare, calls.size());
        assertEquals(585, broad.attribution().get(STABLE).eventBps());
        assertEquals(800, broad.attribution().get(GROWTH).eventBps());
        assertEquals(125, broad.attribution().get(GROWTH).newsBps());
        assertEquals(0, broad.attribution().get(WILDCARD).eventBps());
        var next = engine.accept(initial, broad);
        var adverse = engine.prepare(next, key(2), (r, c, s, i, b) -> b - 1);
        assertEquals(new EventRef(key(2), "Z_ADVERSE"), adverse.event().orElseThrow().reference());
        assertEquals(
                -800, engine.resolve(adverse, List.of()).attribution().get(WILDCARD).eventBps());
        assertEquals(
                new EventRef(key(2), "A_BROAD"),
                engine.prepare(next, key(2), (r, c, s, i, b) -> 0)
                        .event()
                        .orElseThrow()
                        .reference());
        assertThrows(IllegalArgumentException.class, () -> engine.resolve(adverse, reports));
        assertThrows(
                IllegalArgumentException.class,
                () -> new MarketImpact("repeat", STABLE, 1, Channel.FUNDAMENTAL, Optional.of(ref)));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new MarketImpact(
                                "report", STABLE, -1, Channel.REPORT_ONLY, Optional.of(ref)));
        assertThrows(
                IllegalArgumentException.class,
                () -> new MarketImpact("report", STABLE, 0, Channel.REPORT_ONLY, Optional.empty()));
    }

    @Test
    void targetedFavorableEventHasOneAffectedStockAndZeroChanceHasNone() {
        var rules =
                rules(
                        SIDEWAYS,
                        OPEN,
                        0,
                        0,
                        1800,
                        0,
                        1,
                        DEFAULT.profiles(),
                        0,
                        1000,
                        10000,
                        List.of(event("FAVORABLE", 500, GROWTH)));
        var engine = new MarketEngine(rules);
        var first = engine.resolve(engine.prepare(engine.initial(RUN), key(1), CENTRE), List.of());
        assertEquals(575, first.attribution().get(GROWTH).eventBps());
        assertEquals(
                1, first.attribution().values().stream().filter(a -> a.eventBps() != 0).count());

        var none =
                moodProposal(
                        SIDEWAYS,
                        0,
                        1000,
                        (r, c, s, i, b) -> {
                            assertNotEquals(EVENT_SELECTION, c);
                            return 0;
                        });
        assertTrue(none.attribution().values().stream().allMatch(a -> a.eventBps() == 0));
    }

    @Test
    void signedImpactAggregationIsOrderIndependentIsolatedAndImmutable() {
        var engine = new MarketEngine(calmRules(OPEN, 2));
        var initial = engine.initial(RUN);
        var prepared = engine.prepare(initial, key(1), CENTRE);
        var empty = engine.resolve(prepared, List.of());
        assertEquals(0, empty.attribution().get(STABLE).effectiveBps());
        assertEquals(OPEN, empty.proposedRound().moves().get(STABLE).afterTicks());
        var impacts =
                new ArrayList<>(
                        List.of(
                                impact("positive", STABLE, 2500),
                                impact("negative", STABLE, -2000),
                                impact("private-intent", STABLE, 1000)));
        var proposal = engine.resolve(prepared, impacts);
        assertEquals(
                proposal.attribution(), engine.resolve(prepared, impacts.reversed()).attribution());
        impacts.clear();
        assertEquals(1500, proposal.attribution().get(STABLE).effectiveBps());
        assertEquals(empty.attribution().get(GROWTH), proposal.attribution().get(GROWTH));
        assertEquals(
                -150,
                engine.resolve(prepared, List.of(impact("down", STABLE, -150)))
                        .attribution()
                        .get(STABLE)
                        .effectiveBps());
        assertEquals(
                125,
                engine.resolve(prepared, List.of(impact("up", STABLE, 125)))
                        .attribution()
                        .get(STABLE)
                        .effectiveBps());
        assertThrows(UnsupportedOperationException.class, () -> proposal.attribution().clear());
        assertThrows(
                UnsupportedOperationException.class,
                () -> proposal.proposedRound().moves().clear());
        assertEquals(List.of(), initial.snapshot().history());
    }

    @Test
    void signedImpactsCanReverseTheBackgroundAndSaturateMonotonically() {
        var profiles = uniform(new StockProfile(5000, 0, 0, 0, 0));
        var engine =
                new MarketEngine(
                        rules(SIDEWAYS, OPEN, 0, 0, 0, 1800, 2, profiles, 0, 2000, 0, List.of()));
        var state = engine.initial(RUN);
        for (int direction : List.of(-1, 1)) {
            var oppositeBackground =
                    engine.prepare(state, key(1), (r, c, s, i, b) -> b / 2 - 50 * direction);
            assertEquals(
                    -100 * direction,
                    engine.resolve(oppositeBackground, List.of())
                            .proposedRound()
                            .moves()
                            .get(STABLE)
                            .returnBps());
            assertEquals(
                    25 * direction,
                    engine.resolve(
                                    oppositeBackground,
                                    List.of(impact("signal", STABLE, 125 * direction)))
                            .proposedRound()
                            .moves()
                            .get(STABLE)
                            .returnBps());

            int limit = direction > 0 ? 6000 : -5000;
            int background = limit - 100 * direction;
            var nearLimit =
                    engine.prepare(
                            state,
                            key(1),
                            (r, c, s, i, b) -> c == STOCK_NOISE ? b / 2 + background / 2 : b / 2);
            int[] impacts = {0, 50, 200, 3000};
            int[] expected = {background, limit - 50 * direction, limit, limit};
            for (int i = 0; i < impacts.length; i++) {
                assertEquals(
                        expected[i],
                        engine.resolve(
                                        nearLimit,
                                        List.of(impact("signal", STABLE, impacts[i] * direction)))
                                .proposedRound()
                                .moves()
                                .get(STABLE)
                                .returnBps());
            }
        }
    }

    @Test
    void malformedImpactInputsFailWithoutAcceptingAnything() {
        var engine = new MarketEngine(calmRules(OPEN, 2));
        var state = engine.initial(RUN);
        var prepared = engine.prepare(state, key(1), CENTRE);
        var first = impact("duplicate", STABLE, 100);
        assertThrows(
                IllegalArgumentException.class,
                () -> engine.resolve(prepared, List.of(first, first)));
        assertThrows(
                IllegalArgumentException.class,
                () -> engine.resolve(prepared, List.of(first, impact(first.id(), GROWTH, -100))));
        assertThrows(
                IllegalArgumentException.class,
                () -> engine.resolve(prepared, Collections.nCopies(65, first)));
        assertThrows(
                NullPointerException.class,
                () -> engine.resolve(prepared, Collections.singletonList(null)));
        assertThrows(NullPointerException.class, () -> impact("unknown", null, 0));
        assertThrows(IllegalArgumentException.class, () -> impact("oversized", STABLE, -3001));
        assertThrows(IllegalArgumentException.class, () -> impact("", STABLE, 0));
        var nonexistent =
                new MarketImpact(
                        "reference",
                        STABLE,
                        1,
                        Channel.SENTIMENT,
                        Optional.of(new EventRef(key(1), "missing")));
        assertThrows(
                IllegalArgumentException.class,
                () -> engine.resolve(prepared, List.of(nonexistent)));
        assertEquals(0, state.snapshot().sequence());
        assertEquals(
                100,
                engine.resolve(prepared, List.of(first)).attribution().get(STABLE).effectiveBps());
    }

    @Test
    void maximumNoiseAndSignedMomentumAreCappedWithoutBreakingAttribution() {
        var tuning = uniform(new StockProfile(5000, 0, 0, 0, 500));
        var engine =
                new MarketEngine(
                        rules(SIDEWAYS, OPEN, 0, 350, 0, 1800, 2, tuning, 0, 2500, 0, List.of()));
        var initial = engine.initial(RUN);
        for (boolean positive : List.of(true, false)) {
            var prepared = engine.prepare(initial, key(1), (r, c, s, i, b) -> positive ? b - 1 : 0);
            int impact = positive ? 1800 : -1800;
            var outcome = engine.resolve(prepared, List.of(impact("large", STABLE, impact)));
            var parts = outcome.attribution().get(STABLE);
            int limit = positive ? 6000 : -5000;
            assertEquals(positive ? 12500 : -12500, parts.ownBps());
            assertEquals(limit, parts.effectiveBps());
            assertEquals(limit, parts.rawBps() + parts.clampAdjustmentBps());
            assertEquals(
                    parts.rawBps(),
                    parts.moodBps()
                            + parts.commonBps()
                            + parts.ownBps()
                            + parts.momentumBps()
                            + parts.eventBps()
                            + parts.newsBps());
            var saturated =
                    engine.resolve(
                            prepared, List.of(impact("larger", STABLE, positive ? 3000 : -3000)));
            assertEquals(parts, saturated.attribution().get(STABLE));
            var next = engine.accept(initial, outcome);
            var momentum = engine.resolve(engine.prepare(next, key(2), CENTRE), List.of());
            assertEquals(positive ? 350 : -350, momentum.attribution().get(STABLE).momentumBps());
        }
    }

    @Test
    void campaignWorstCasesAndHistoryEvictionPreservePricesAcrossStages() {
        var tuning = uniform(new StockProfile(5000, 0, 0, 0, 0));
        var engine =
                new MarketEngine(
                        rules(SIDEWAYS, OPEN, 0, 0, 0, 0, 3, tuning, 0, 2000, 0, List.of()));
        for (boolean gains : List.of(true, false)) {
            var current = engine.initial(RUN);
            for (int round = 1; round <= 21; round++) {
                int ordinal = round;
                var proposal =
                        engine.resolve(
                                engine.prepare(
                                        current,
                                        key(round),
                                        (r, c, s, i, b) -> {
                                            assertEquals(ordinal, r);
                                            return gains ? b - 1 : 0;
                                        }),
                                List.of());
                assertEquals(
                        current.snapshot().quotes().get(STABLE).indexTicks(),
                        proposal.proposedRound().moves().get(STABLE).beforeTicks());
                current = engine.accept(current, proposal);
                assertEquals(round, current.snapshot().sequence());
                assertEquals(Math.min(round, 3), current.snapshot().history().size());
            }
            assertEquals(key(19), current.snapshot().history().getFirst().key());
            assertEquals(key(21), current.snapshot().history().getLast().key());
            assertEquals(
                    gains ? 19_342_813_113_834_086L : 476_838L,
                    current.snapshot().quotes().get(STABLE).indexTicks());
            assertEquals(
                    gains ? 6000 : -5000,
                    current.snapshot().quotes().get(STABLE).lastReturnBps().orElseThrow());
        }
    }

    @Test
    void numericalLimitLeavesStateUntouchedAndSameContextCanBeRetried() {
        for (long limit : List.of(MarketMath.MIN_INDEX, MarketMath.MAX_INDEX)) {
            var engine = new MarketEngine(calmRules(limit, 2));
            var state = engine.initial(RUN);
            var prepared = engine.prepare(state, key(1), CENTRE);
            int impact = limit == MarketMath.MIN_INDEX ? -1 : 1;
            assertThrows(
                    ArithmeticException.class,
                    () -> engine.resolve(prepared, List.of(impact("limit", WILDCARD, impact))));
            assertSame(state.snapshot(), prepared.planning().accepted());
            assertTrue(state.snapshot().history().isEmpty());
            assertEquals(
                    limit,
                    engine.accept(state, engine.resolve(prepared, List.of()))
                            .snapshot()
                            .quotes()
                            .get(WILDCARD)
                            .indexTicks());
        }
    }

    @Test
    void acceptingRequiresTheExactPredecessorAndAnImmediateRoundUnderTheSameRules() {
        var engine = new MarketEngine(calmRules(OPEN, 2));
        var foreign = new MarketEngine(calmRules(OPEN, 3));
        var state = engine.initial(RUN);
        assertThrows(IllegalArgumentException.class, () -> engine.prepare(state, key(2), CENTRE));
        assertThrows(
                IllegalArgumentException.class,
                () -> engine.prepare(state, new RoundKey(new RunId(2), 1, 1), CENTRE));
        assertThrows(IllegalArgumentException.class, () -> foreign.prepare(state, key(1), CENTRE));

        var prepared = engine.prepare(state, key(1), CENTRE);
        assertThrows(IllegalArgumentException.class, () -> foreign.resolve(prepared, List.of()));
        var proposal = engine.resolve(prepared, List.of());
        assertThrows(IllegalArgumentException.class, () -> foreign.accept(state, proposal));
        assertThrows(
                IllegalArgumentException.class, () -> engine.accept(engine.initial(RUN), proposal));
        var accepted = engine.accept(state, proposal);
        assertEquals(
                MarketRules.STOCKS,
                List.copyOf(accepted.snapshot().history().getFirst().moves().keySet()));
        assertEquals(1, accepted.snapshot().sequence());
        assertThrows(
                UnsupportedOperationException.class, () -> accepted.snapshot().history().clear());
        assertThrows(IllegalArgumentException.class, () -> engine.accept(accepted, proposal));
        assertThrows(
                IllegalArgumentException.class, () -> engine.prepare(accepted, key(1), CENTRE));
    }

    private static MarketEngine.Resolution moodProposal(
            MarketRegime mood, int drift, int factor, MarketDraws draws) {
        var engine =
                new MarketEngine(
                        rules(
                                mood,
                                OPEN,
                                700,
                                350,
                                1800,
                                1800,
                                2,
                                DEFAULT.profiles(),
                                drift,
                                factor,
                                0,
                                List.of()));
        return engine.resolve(engine.prepare(engine.initial(RUN), key(1), draws), List.of());
    }

    private static MarketRules calmRules(long index, int history) {
        return rules(SIDEWAYS, index, 0, 0, 0, 1800, history, uniform(ZERO), 0, 1000, 0, List.of());
    }

    private static MarketRules rules(
            MarketRegime mood,
            long index,
            int common,
            int momentum,
            int events,
            int news,
            int capacity,
            Map<StockId, StockProfile> profiles,
            int drift,
            int volatility,
            int chance,
            List<MarketRules.Event> catalog) {
        Map<MarketRegime, Integer> transitions =
                MarketRules.marketMap(
                        MarketRules.REGIMES, candidate -> candidate == mood ? 10000 : 0);
        var regimes =
                MarketRules.REGIMES.stream()
                        .map(
                                id ->
                                        new MarketRules.Regime(
                                                id, drift, volatility, chance, transitions))
                        .toList();
        return new MarketRules(
                "controlled",
                settings(index, mood, common, momentum, events, news, capacity),
                profiles,
                regimes,
                catalog);
    }

    private static MarketRules.Settings settings(
            long index,
            MarketRegime mood,
            int common,
            int momentum,
            int event,
            int news,
            int capacity) {
        return new MarketRules.Settings(
                index, mood, common, momentum, event, news, -5000, 6000, capacity);
    }

    private static Map<StockId, StockProfile> uniform(StockProfile profile) {
        return MarketRules.marketMap(MarketRules.STOCKS, stock -> profile);
    }

    private static RoundKey key(int ordinal) {
        return new RoundKey(RUN, (ordinal - 1) / 3 + 1, (ordinal - 1) % 3 + 1);
    }

    private static MarketImpact impact(String id, StockId stock, int bps) {
        return new MarketImpact(id, stock, bps, Channel.SENTIMENT, Optional.empty());
    }

    private static MarketRules.Event event(String id, int shock, StockId... stocks) {
        return new MarketRules.Event(
                id, MarketRules.EventCategory.SECTOR, Set.of(stocks), shock, 1);
    }
}
