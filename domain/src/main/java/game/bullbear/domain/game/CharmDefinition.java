package game.bullbear.domain.game;

import game.bullbear.domain.charm.CharmCategory;
import java.util.Objects;

public record CharmDefinition(Id id, CharmCategory category, SpecialCurrencyAmount price) {
    public CharmDefinition {
        Objects.requireNonNull(id);
        Objects.requireNonNull(category);
        if (price.units() == 0) {
            throw new IllegalArgumentException("Charm price must be positive");
        }
    }

    public record Id(String value) {
        public Id {
            if (value.isBlank()) {
                throw new IllegalArgumentException("Charm definition ID is required");
            }
        }
    }

    public record InstanceId(long value) {
        public InstanceId {
            if (value < 1) {
                throw new IllegalArgumentException("Charm instance ID must be positive");
            }
        }
    }

    public record Owned(InstanceId instanceId, Id definitionId) {
        public Owned {
            Objects.requireNonNull(instanceId);
            Objects.requireNonNull(definitionId);
        }
    }
}
