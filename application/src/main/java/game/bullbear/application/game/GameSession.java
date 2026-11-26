package game.bullbear.application.game;

import game.bullbear.domain.game.GameCatalog;
import game.bullbear.domain.game.GameRules;
import game.bullbear.domain.game.GameRun;
import game.bullbear.domain.game.GameSnapshot;
import game.bullbear.domain.game.RunId;
import java.util.Objects;
import java.util.Optional;

/** In-memory session composed with explicit configuration by trusted application code. */
public final class GameSession {
    private final GameRules rules;
    private final GameCatalog catalog;
    private GameRun run;

    public GameSession(GameRules rules, GameCatalog catalog) {
        this.rules = Objects.requireNonNull(rules);
        this.catalog = Objects.requireNonNull(catalog);
    }

    public GameSnapshot start(RunId id) {
        Objects.requireNonNull(id);
        if (run != null) {
            var previous = run.snapshot();
            if (!previous.phase().terminal() || id.value() <= previous.key().runId().value()) {
                throw new IllegalStateException("Start requires a terminal run and a new run ID");
            }
        }
        var next = GameRun.start(id, rules, catalog);
        var initial = next.snapshot();
        run = next;
        return initial;
    }

    public Optional<GameSnapshot> view() {
        return Optional.ofNullable(run).map(GameRun::snapshot);
    }

    // Reserved for the trusted application coordinator. Presentation receives snapshots only.
    GameRun activeRun() {
        if (run == null) {
            throw new IllegalStateException("No run has been started");
        }
        return run;
    }
}
