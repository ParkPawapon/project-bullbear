package game.bullbear.domain.market;

import static game.bullbear.domain.market.MarketRegime.*;
import static game.bullbear.domain.market.MarketRules.EventCategory.*;
import static game.bullbear.domain.stock.StockId.*;

import game.bullbear.domain.stock.StockId;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Provisional fictional game tuning. A content change creates a different replay identity. */
public final class MarketTuning {
    private static final MarketRules PROVISIONAL =
            new MarketRules(
                    "phase03-v1",
                    new MarketRules.Settings(
                            1_000_000_000_000L, SIDEWAYS, 700, 350, 1800, 1800, -5000, 6000, 64),
                    Map.of(
                            STABLE, new StockProfile(650, 550, 650, 650, 50),
                            GROWTH, new StockProfile(1350, 950, 1100, 1150, 130),
                            VOLATILE, new StockProfile(1850, 850, 950, 1300, 70),
                            FOLLOWER, new StockProfile(950, 1400, 1300, 950, 90),
                            WILDCARD, new StockProfile(2500, 750, 850, 1450, 100)),
                    List.of(
                            regime(BULL, 320, 1000, 1800, 5400, 800, 2200, 1200, 300, 100),
                            regime(BEAR, -320, 1150, 2000, 700, 4700, 2300, 1200, 800, 300),
                            regime(SIDEWAYS, 0, 900, 1400, 1900, 1800, 4900, 1100, 200, 100),
                            regime(
                                    HIGH_VOLATILITY,
                                    0,
                                    1550,
                                    2600,
                                    1400,
                                    1500,
                                    2200,
                                    3400,
                                    1000,
                                    500),
                            regime(CRASH, -700, 1750, 2200, 300, 1400, 1100, 1800, 1500, 3900),
                            regime(RECOVERY, 450, 1250, 1800, 2900, 700, 2100, 1000, 300, 3000)),
                    List.of(
                            event("RATE_TIGHTENING", MACRO, -520, GROWTH, FOLLOWER, WILDCARD),
                            event("RATE_EASING", MACRO, 520, GROWTH, FOLLOWER, WILDCARD),
                            event("TECH_BREAKTHROUGH", SECTOR, 950, GROWTH),
                            event("TECH_SETBACK", SECTOR, -850, GROWTH),
                            event("ENERGY_SUPPLY_TIGHT", SECTOR, 950, VOLATILE),
                            event("ENERGY_DEMAND_DROP", SECTOR, -850, VOLATILE),
                            event("CREDIT_STRESS", SECTOR, -850, FOLLOWER),
                            event("CREDIT_RECOVERY", SECTOR, 750, FOLLOWER),
                            event("CONSUMER_RESILIENCE", SECTOR, 520, STABLE),
                            event("CONSUMER_DISRUPTION", SECTOR, -520, STABLE),
                            event("SPECULATIVE_SURGE", SENTIMENT, 1000, WILDCARD),
                            event("SPECULATIVE_UNWIND", SENTIMENT, -1000, WILDCARD),
                            event(
                                    "SUPPLY_CHAIN_STRESS",
                                    CROSS_SECTOR,
                                    -650,
                                    GROWTH,
                                    VOLATILE,
                                    STABLE),
                            event(
                                    "PRODUCTIVITY_UPLIFT",
                                    CROSS_SECTOR,
                                    650,
                                    GROWTH,
                                    STABLE,
                                    FOLLOWER),
                            event(
                                    "MARKET_PANIC",
                                    BROAD_MARKET,
                                    -850,
                                    STABLE,
                                    GROWTH,
                                    VOLATILE,
                                    FOLLOWER,
                                    WILDCARD),
                            event(
                                    "MARKET_RELIEF",
                                    BROAD_MARKET,
                                    750,
                                    STABLE,
                                    GROWTH,
                                    VOLATILE,
                                    FOLLOWER,
                                    WILDCARD)));

    private MarketTuning() {}

    public static MarketRules provisional() {
        return PROVISIONAL;
    }

    private static MarketRules.Regime regime(
            MarketRegime id,
            int drift,
            int volatility,
            int eventChance,
            int bull,
            int bear,
            int sideways,
            int highVolatility,
            int crash,
            int recovery) {
        return new MarketRules.Regime(
                id,
                drift,
                volatility,
                eventChance,
                Map.of(
                        BULL,
                        bull,
                        BEAR,
                        bear,
                        SIDEWAYS,
                        sideways,
                        HIGH_VOLATILITY,
                        highVolatility,
                        CRASH,
                        crash,
                        RECOVERY,
                        recovery));
    }

    private static MarketRules.Event event(
            String id, MarketRules.EventCategory category, int shock, StockId... targets) {
        return new MarketRules.Event(id, category, Set.of(targets), shock, 1);
    }
}
