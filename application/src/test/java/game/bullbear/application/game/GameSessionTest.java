package game.bullbear.application.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import game.bullbear.domain.game.CoinAmount;
import game.bullbear.domain.game.GameCatalog;
import game.bullbear.domain.game.GamePhase;
import game.bullbear.domain.game.GameRules;
import game.bullbear.domain.game.Investment;
import game.bullbear.domain.game.NewsClaim;
import game.bullbear.domain.game.RoundEntry;
import game.bullbear.domain.game.RoundKey;
import game.bullbear.domain.game.RunId;
import game.bullbear.domain.game.SpecialCurrencyAmount;
import game.bullbear.domain.news.NewsSourceId;
import game.bullbear.domain.stock.StockId;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class GameSessionTest {
    private static final CoinAmount ONE = new CoinAmount(1);

    @Test
    void publicSessionReturnsSnapshotsAndRejectsAnActiveReplacement() {
        var session = session();
        assertTrue(session.view().isEmpty());
        assertThrows(IllegalStateException.class, session::activeRun);
        assertThrows(NullPointerException.class, () -> session.start(null));
        var start = session.start(new RunId(1));
        assertEquals(GamePhase.ROUND_ENTRY, start.phase());
        assertEquals(start, session.view().orElseThrow());
        assertThrows(IllegalStateException.class, () -> session.start(new RunId(2)));
        assertEquals(start, session.view().orElseThrow());
    }

    @Test
    void trustedCoordinatorCanResolveRoundsAndRestartWithAnIndependentRun() {
        var session = session();
        var first = session.start(new RunId(10));
        var run = session.activeRun();
        var movements = new EnumMap<StockId, Investment.Movement>(StockId.class);
        for (var stock : StockId.values()) {
            movements.put(stock, Investment.Movement.DOWN);
        }
        for (int round = 1; round <= RoundKey.ROUNDS_PER_STAGE; round++) {
            var key = session.view().orElseThrow().key();
            var hidden =
                    new NewsClaim(
                            new NewsClaim.Id("app-fixture-hidden"),
                            NewsSourceId.D,
                            StockId.STABLE,
                            NewsClaim.Polarity.NEGATIVE);
            run.enter(new RoundEntry(key, CoinAmount.ZERO, CoinAmount.ZERO, List.of(hidden)));
            var planning = session.view().orElseThrow();
            assertTrue(planning.visibleNews().isEmpty());
            assertFalse(planning.toString().contains("app-fixture-hidden"));
            run.commit(key, new Investment.Allocation(Map.of(StockId.STABLE, ONE)));
            var committed = session.view().orElseThrow().commitment().orElseThrow();
            run.settle(
                    new Investment.Settlement(
                            committed,
                            movements,
                            Map.of(StockId.STABLE, ONE),
                            SpecialCurrencyAmount.ZERO,
                            Optional.empty()));
            if (round < RoundKey.ROUNDS_PER_STAGE) {
                run.continueRound(key);
            } else {
                run.closeStage(key, Optional.empty());
            }
        }
        assertEquals(GamePhase.GAME_OVER, session.view().orElseThrow().phase());
        assertThrows(IllegalStateException.class, () -> session.start(new RunId(10)));
        assertThrows(IllegalStateException.class, () -> session.start(new RunId(9)));
        var restarted = session.start(new RunId(11));
        assertEquals(first.wallet(), restarted.wallet());
        assertEquals(GamePhase.ROUND_ENTRY, restarted.phase());
        assertEquals(new RoundKey(new RunId(11), 1, 1), restarted.key());
        assertTrue(restarted.result().isEmpty());
        assertTrue(restarted.visibleNews().isEmpty());
        assertTrue(restarted.charms().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> restarted.visibleSources().clear());
        assertEquals(GamePhase.ROUND_ENTRY, first.phase());
        assertEquals(GamePhase.GAME_OVER, run.snapshot().phase());
    }

    private static GameSession session() {
        var goals =
                IntStream.rangeClosed(1, RoundKey.CAMPAIGN_STAGES)
                        .mapToObj(CoinAmount::new)
                        .toList();
        var rules =
                new GameRules(
                        goals,
                        new CoinAmount(4),
                        SpecialCurrencyAmount.ZERO,
                        Set.of(NewsSourceId.A, NewsSourceId.B, NewsSourceId.C),
                        1,
                        1);
        var sources =
                Arrays.stream(NewsSourceId.values())
                        .map(id -> new GameCatalog.SourceDefinition(id, 0, 0))
                        .toList();
        return new GameSession(rules, new GameCatalog(sources, List.of()));
    }
}
