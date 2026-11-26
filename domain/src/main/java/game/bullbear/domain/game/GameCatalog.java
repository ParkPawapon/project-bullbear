package game.bullbear.domain.game;

import game.bullbear.domain.news.NewsSourceId;
import game.bullbear.domain.stock.StockId;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Explicit structural definitions shared across runs. Stock IDs are the current stock definitions.
 */
public final class GameCatalog {
    private final Map<NewsSourceId, SourceDefinition> sources;
    private final Map<CharmDefinition.Id, CharmDefinition> charms;

    public GameCatalog(
            List<SourceDefinition> sourceDefinitions, List<CharmDefinition> charmDefinitions) {
        var bySource = new EnumMap<NewsSourceId, SourceDefinition>(NewsSourceId.class);
        for (var source : sourceDefinitions) {
            if (bySource.putIfAbsent(source.id(), source) != null) {
                throw new IllegalArgumentException("Duplicate source definition");
            }
        }
        if (bySource.size() != NewsSourceId.values().length) {
            throw new IllegalArgumentException("One definition per news source is required");
        }
        var byCharm = new LinkedHashMap<CharmDefinition.Id, CharmDefinition>();
        for (var charm : charmDefinitions) {
            if (byCharm.putIfAbsent(charm.id(), charm) != null) {
                throw new IllegalArgumentException("Duplicate charm definition");
            }
        }
        sources = Collections.unmodifiableMap(bySource);
        charms = Collections.unmodifiableMap(byCharm);
    }

    public List<StockId> stocks() {
        return List.of(StockId.values());
    }

    public Map<NewsSourceId, SourceDefinition> sources() {
        return sources;
    }

    public Map<CharmDefinition.Id, CharmDefinition> charms() {
        return charms;
    }

    void requireCharm(CharmDefinition.Id id) {
        if (!charms.containsKey(id)) {
            throw new IllegalArgumentException("Unknown charm definition");
        }
    }

    /** Credibility is a probability. Impact is a nonnegative magnitude in the same scale. */
    public record SourceDefinition(
            NewsSourceId id, int credibilityBasisPoints, long impactBasisPoints) {
        private static final int CERTAINTY_BASIS_POINTS = 10_000;

        public SourceDefinition {
            Objects.requireNonNull(id);
            if (credibilityBasisPoints < 0
                    || credibilityBasisPoints > CERTAINTY_BASIS_POINTS
                    || impactBasisPoints < 0) {
                throw new IllegalArgumentException("Invalid source credibility or impact");
            }
        }
    }
}
