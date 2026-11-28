package game.bullbear.application.market;

import game.bullbear.domain.game.RoundKey;
import game.bullbear.domain.game.RunId;
import game.bullbear.domain.market.MarketEngine;
import game.bullbear.domain.market.MarketEngine.PreparedRound;
import game.bullbear.domain.market.MarketEngine.Resolution;
import game.bullbear.domain.market.MarketImpact;
import game.bullbear.domain.market.MarketRules;
import game.bullbear.domain.market.MarketSnapshot;
import game.bullbear.domain.market.MarketState;
import java.util.List;

/** Headless owner of accepted market state. Settlement remains with GameRun. */
public final class MarketSession {
    private final MarketEngine engine;
    private final MarketRandom random;
    private MarketState state;

    public MarketSession(RunId runId, long rootSeed, MarketRules rules) {
        engine = new MarketEngine(rules);
        random = new MarketRandom(rootSeed, rules);
        state = engine.initial(runId);
    }

    public synchronized MarketSnapshot view() {
        return state.snapshot();
    }

    public synchronized PreparedRound prepare(RoundKey key) {
        return engine.prepare(state, key, random);
    }

    public Resolution resolve(PreparedRound prepared, List<MarketImpact> impacts) {
        return engine.resolve(prepared, impacts);
    }

    public synchronized MarketSnapshot accept(Resolution proposal) {
        state = engine.accept(state, proposal);
        return state.snapshot();
    }
}
