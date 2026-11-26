package game.bullbear.domain.game;

import game.bullbear.domain.charm.CharmCategory;
import game.bullbear.domain.news.NewsSourceId;
import game.bullbear.domain.stock.StockId;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

final class GameFixtures {
    static final CharmDefinition.Id CHARM = new CharmDefinition.Id("fixture-charm");
    static final Set<NewsSourceId> SOURCES = Set.of(NewsSourceId.A, NewsSourceId.B, NewsSourceId.C);
    static final CharmDefinition DEFINITION =
            new CharmDefinition(CHARM, CharmCategory.INFORMATION, special(2));

    private GameFixtures() {}

    static CoinAmount coin(long value) {
        return new CoinAmount(value);
    }

    static SpecialCurrencyAmount special(long value) {
        return new SpecialCurrencyAmount(value);
    }

    static CharmDefinition.Owned owned(long id) {
        return new CharmDefinition.Owned(new CharmDefinition.InstanceId(id), CHARM);
    }

    static List<CoinAmount> goals() {
        return IntStream.rangeClosed(1, RoundKey.CAMPAIGN_STAGES)
                .mapToObj(GameFixtures::coin)
                .toList();
    }

    static List<GameCatalog.SourceDefinition> sources() {
        return Arrays.stream(NewsSourceId.values())
                .map(source -> new GameCatalog.SourceDefinition(source, 5_000, 120))
                .toList();
    }

    static GameCatalog catalog() {
        return new GameCatalog(sources(), List.of(DEFINITION));
    }

    static GameRules rules(long wallet, long special) {
        return new GameRules(goals(), coin(wallet), special(special), SOURCES, 2, 2);
    }

    static GameRun run(long wallet, long special) {
        return GameRun.start(new RunId(1), rules(wallet, special), catalog());
    }

    static RoundEntry entry(RoundKey key, long interest, long minimum, NewsClaim... claims) {
        return new RoundEntry(key, coin(interest), coin(minimum), List.of(claims));
    }

    static RoundKey key(GameRun run) {
        return run.snapshot().key();
    }

    static GameRun planning(long wallet, long special) {
        var run = run(wallet, special);
        run.enter(entry(key(run), 0, 0));
        return run;
    }

    static Investment.Allocation allocate(long stake) {
        return new Investment.Allocation(Map.of(StockId.STABLE, coin(stake)));
    }

    static Map<StockId, Investment.Movement> movements() {
        var result = new EnumMap<StockId, Investment.Movement>(StockId.class);
        for (var stock : StockId.values()) {
            result.put(
                    stock,
                    stock == StockId.STABLE ? Investment.Movement.UP : Investment.Movement.DOWN);
        }
        return result;
    }

    static Investment.Settlement settlement(GameRun run, long payout, long reward) {
        return settlement(run, payout, reward, Optional.empty());
    }

    static Investment.Settlement settlement(
            GameRun run, long payout, long reward, Optional<Shop.Rotation> rotation) {
        return new Investment.Settlement(
                run.snapshot().commitment().orElseThrow(),
                movements(),
                Map.of(StockId.STABLE, coin(payout)),
                special(reward),
                rotation);
    }

    static void resolve(GameRun run, long stake, long payout) {
        run.commit(key(run), allocate(stake));
        run.settle(settlement(run, payout, 0));
    }
}
