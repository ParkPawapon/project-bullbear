package game.bullbear.application.market;

import static game.bullbear.domain.market.MarketDraws.Channel.*;
import static game.bullbear.domain.stock.StockId.STABLE;
import static org.junit.jupiter.api.Assertions.*;

import game.bullbear.domain.game.RoundKey;
import game.bullbear.domain.game.RunId;
import game.bullbear.domain.market.MarketImpact;
import game.bullbear.domain.market.MarketRegime;
import game.bullbear.domain.market.MarketRules;
import game.bullbear.domain.market.MarketSnapshot.PriceMove;
import game.bullbear.domain.market.MarketTuning;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

final class MarketSessionTest {
    private static final MarketRules RULES = MarketTuning.provisional();
    private static final RunId RUN = new RunId(1);

    @Test
    void goldenVectorCoversAddressEncodingRngEventAndFiveSignedReturns() {
        assertEquals(
                "44030c16db9dfd0ebd240bc040753e473c6edfd7ab98434e44987285a6e44fa9",
                RULES.fingerprint());
        // Expected values calculated independently with SHA-256 and Java Random's specified 48-bit
        // LCG.
        var random = new MarketRandom(Long.MIN_VALUE, RULES);
        assertEquals(923, random.draw(1, COMMON_NOISE, "", 0, 1401));
        assertEquals(3473, random.draw(1, STOCK_NOISE, "WILDCARD", 1, 5001));
        assertEquals(3301, random.draw(4, REGIME_TRANSITION, "SIDEWAYS", 0, 10000));
        assertEquals(1333, random.draw(1, EVENT_OCCURRENCE, "", 0, 10000));
        assertEquals(8, random.draw(1, EVENT_SELECTION, "", 0, 16));
        assertEquals(1188, new MarketRandom(-42, RULES).draw(1, COMMON_NOISE, "", 0, 1401));

        var session = new MarketSession(RUN, Long.MIN_VALUE, RULES);
        var prepared = session.prepare(key(RUN, 1));
        assertEquals(MarketRegime.SIDEWAYS, prepared.planning().mood());
        assertEquals(
                "PRODUCTIVITY_UPLIFT", prepared.event().orElseThrow().reference().definitionId());
        var proposal = session.resolve(prepared, List.of());
        assertEquals(
                List.of(327, 71, -64, 851, 1054),
                proposal.proposedRound().moves().values().stream()
                        .map(PriceMove::returnBps)
                        .toList());
        assertEquals(
                new PriceMove(1_000_000_000_000L, 1_032_700_000_000L, 327),
                proposal.proposedRound().moves().get(STABLE));
        assertEquals(proposal.proposedRound(), session.accept(proposal).history().getFirst());
    }

    @Test
    void drawsAreAddressedAndValidateBasicCoordinates() {
        var random = new MarketRandom(42, RULES);
        assertEquals(410, random.draw(1, COMMON_NOISE, "", 0, 1401));
        assertEquals(925, random.draw(1, STOCK_NOISE, "WILDCARD", 1, 5001));
        assertEquals(8767, random.draw(4, REGIME_TRANSITION, "SIDEWAYS", 0, 10000));
        int otherSubject = random.draw(1, STOCK_NOISE, "STABLE", 1, 5001);
        assertNotEquals(925, otherSubject);
        assertNotEquals(410, random.draw(4, COMMON_NOISE, "", 0, 1401));
        assertEquals(925, random.draw(1, STOCK_NOISE, "WILDCARD", 1, 5001));
        assertEquals(410, random.draw(1, COMMON_NOISE, "", 0, 1401));
        assertThrows(
                IllegalArgumentException.class, () -> random.draw(0, COMMON_NOISE, "", 0, 1401));
        assertThrows(
                IllegalArgumentException.class, () -> random.draw(1, COMMON_NOISE, "", -1, 1401));
        assertThrows(IllegalArgumentException.class, () -> random.draw(1, COMMON_NOISE, "", 0, 0));
    }

    @Test
    void sameSeedReplaysAcrossRunIdsStageBoundariesAndExtraReads() {
        var otherRun = new RunId(2);
        var first = new MarketSession(RUN, -42, RULES);
        var reorderedRules =
                new MarketRules(
                        RULES.version(),
                        RULES.settings(),
                        RULES.profiles(),
                        new ArrayList<>(RULES.regimes().values()).reversed(),
                        RULES.events().reversed());
        var replay = new MarketSession(otherRun, -42, reorderedRules);
        for (int ordinal = 1; ordinal <= 5; ordinal++) {
            var left = first.prepare(key(RUN, ordinal));
            var starting = replay.view();
            var right = replay.prepare(key(otherRun, ordinal));
            assertEquals(starting, right.planning().accepted());
            assertEquals(right.planning(), replay.prepare(key(otherRun, ordinal)).planning());
            var result = first.resolve(left, List.of());
            var replayed = replay.resolve(right, List.of());
            assertEquals(starting, replay.view());
            assertEquals(result.attribution(), replayed.attribution());
            assertEquals(result.proposedRound().moves(), replayed.proposedRound().moves());
            assertEquals(
                    left.event().map(e -> e.definition().id()),
                    right.event().map(e -> e.definition().id()));
            var accepted = replay.accept(replayed);
            assertEquals(ordinal, accepted.sequence());
            assertEquals(key(otherRun, ordinal), accepted.history().getLast().key());
            assertEquals(first.accept(result).quotes(), accepted.quotes());
        }
        var fresh = new MarketSession(new RunId(3), -42, RULES).view();
        assertEquals(MarketRegime.SIDEWAYS, fresh.regime());
        assertEquals(0, fresh.sequence());
        assertTrue(fresh.history().isEmpty());
        assertTrue(fresh.quotes().values().stream().allMatch(q -> q.lastReturnBps().isEmpty()));
    }

    @Test
    void onlyOneProposalCanAdvanceTheOwnerAndNoOtherRunCanPromoteIt() {
        var session = new MarketSession(RUN, 42, RULES);
        var unrelated = new MarketSession(new RunId(2), 42, RULES);
        var prepared = session.prepare(key(RUN, 1));
        var baseline = session.resolve(prepared, List.of());
        var withSentiment =
                session.resolve(
                        prepared,
                        List.of(
                                new MarketImpact(
                                        "later",
                                        STABLE,
                                        500,
                                        MarketImpact.Channel.SENTIMENT,
                                        Optional.empty())));
        assertEquals(264, withSentiment.proposedRound().moves().get(STABLE).returnBps());
        assertEquals(0, session.view().sequence());
        assertThrows(IllegalArgumentException.class, () -> unrelated.accept(baseline));
        assertSame(prepared.planning().accepted(), session.view());
        var accepted = session.accept(withSentiment);
        assertThrows(IllegalArgumentException.class, () -> session.accept(withSentiment));
        assertThrows(IllegalArgumentException.class, () -> session.accept(baseline));
        assertSame(accepted, session.view());
        assertEquals(1, accepted.history().size());
    }

    @Test
    void fixedSeedSampleHasMoreThanOneMarketPath() {
        var outcomes = new HashSet<>();
        for (long seed = 0; seed < 16; seed++) {
            var run = new MarketSession(RUN, seed, RULES);
            outcomes.add(run.resolve(run.prepare(key(RUN, 1)), List.of()).proposedRound().moves());
        }
        assertTrue(outcomes.size() > 1);
    }

    static RoundKey key(RunId run, int ordinal) {
        int roundsPerStage = RoundKey.ROUNDS_PER_STAGE;
        return new RoundKey(
                run, (ordinal - 1) / roundsPerStage + 1, (ordinal - 1) % roundsPerStage + 1);
    }
}
