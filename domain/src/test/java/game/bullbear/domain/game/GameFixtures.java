package game.bullbear.domain.game;

import game.bullbear.domain.charm.CharmCategory;
import game.bullbear.domain.news.NewsSourceId;
import java.util.Arrays;
import java.util.List;
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
}
