package game.bullbear.domain.game;

import static game.bullbear.domain.game.GameRejection.Reason.STALE_INPUT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Order defines the stable one-based slot IDs. A revision identifies the offer rotation. */
public record Shop(long revision, List<Optional<CharmDefinition.Id>> slots) {
    public Shop {
        if (revision < 0) {
            throw new IllegalArgumentException("Negative shop revision");
        }
        slots = List.copyOf(slots);
        GameRules.validateCapacity(slots.size());
    }

    static Shop empty(int capacity) {
        return new Shop(0, Collections.nCopies(capacity, Optional.empty()));
    }

    Shop consume(long expectedRevision, SlotId slot, CharmDefinition.Id expectedOffer) {
        int index = Objects.checkIndex(slot.number() - 1, slots.size());
        Objects.requireNonNull(expectedOffer);
        if (revision != expectedRevision
                || slots.get(index).filter(expectedOffer::equals).isEmpty()) {
            throw new GameRejection(STALE_INPUT);
        }
        var remaining = new ArrayList<>(slots);
        remaining.set(index, Optional.empty());
        return new Shop(revision, remaining);
    }

    Shop rotate(Rotation rotation, GameCatalog catalog) {
        if (rotation.previousRevision() != revision) {
            throw new GameRejection(STALE_INPUT);
        }
        validateOffers(catalog, rotation.slots(), slots.size());
        return new Shop(Math.incrementExact(revision), rotation.slots());
    }

    static void validateOffers(
            GameCatalog catalog, List<Optional<CharmDefinition.Id>> offers, int capacity) {
        if (offers.size() != capacity) {
            throw new IllegalArgumentException("Replacement must cover every shop slot");
        }
        for (var offer : offers) {
            offer.ifPresent(catalog::requireCharm);
        }
    }

    public record SlotId(int number) {
        public SlotId {
            if (number < 1) {
                throw new IllegalArgumentException("Shop slot ID must be positive");
            }
        }
    }

    public record Rotation(long previousRevision, List<Optional<CharmDefinition.Id>> slots) {
        public Rotation {
            if (previousRevision < 0) {
                throw new IllegalArgumentException("Negative shop revision");
            }
            slots = List.copyOf(slots);
            GameRules.validateCapacity(slots.size());
        }
    }
}
