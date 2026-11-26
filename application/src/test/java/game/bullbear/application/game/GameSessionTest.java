package game.bullbear.application.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import game.bullbear.domain.game.CoinAmount;
import game.bullbear.domain.game.GameCatalog;
import game.bullbear.domain.game.GamePhase;
import game.bullbear.domain.game.GameRules;
import game.bullbear.domain.game.RoundKey;
import game.bullbear.domain.game.RunId;
import game.bullbear.domain.game.SpecialCurrencyAmount;
import game.bullbear.domain.news.NewsSourceId;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class GameSessionTest {
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
