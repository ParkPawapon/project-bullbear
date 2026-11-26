package game.bullbear.domain.game;

import game.bullbear.domain.news.NewsSourceId;
import game.bullbear.domain.stock.StockId;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Player-safe immutable projection. Round entry inputs stay with the trusted domain. */
public record GameSnapshot(
        RoundKey key,
        GamePhase phase,
        CoinAmount goal,
        CoinAmount wallet,
        CoinAmount bank,
        SpecialCurrencyAmount special,
        Set<NewsSourceId> visibleSources,
        List<NewsClaim> visibleNews,
        List<CharmDefinition.Owned> charms,
        Shop shop,
        Optional<Investment.Commitment> commitment,
        Optional<Result> result) {
    public GameSnapshot {
        visibleSources = Collections.unmodifiableSet(EnumSet.copyOf(visibleSources));
        visibleNews = List.copyOf(visibleNews);
        charms = List.copyOf(charms);
        Objects.requireNonNull(commitment);
        Objects.requireNonNull(result);
    }

    public boolean campaignWon() {
        return key.endless()
                || (key.stage() == RoundKey.CAMPAIGN_STAGES
                        && (phase == GamePhase.VICTORY || phase == GamePhase.FINISHED));
    }

    public record Result(
            Investment.Commitment investment,
            Map<StockId, Investment.Movement> movements,
            CoinAmount returned,
            SpecialCurrencyAmount reward,
            CoinAmount walletAfter) {
        public Result {
            var copy = new EnumMap<StockId, Investment.Movement>(StockId.class);
            copy.putAll(Map.copyOf(movements));
            movements = Collections.unmodifiableMap(copy);
        }

        public long profitOrLoss() {
            return Math.subtractExact(returned.units(), investment.allocation().total().units());
        }
    }
}
