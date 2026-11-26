package game.bullbear.domain.game;

import game.bullbear.domain.news.NewsSourceId;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record GameRules(
        List<CoinAmount> campaignGoals,
        CoinAmount startingCoin,
        SpecialCurrencyAmount startingSpecial,
        Set<NewsSourceId> initialSources,
        int inventoryCapacity,
        int shopSlots) {
    public static final int INITIAL_SOURCE_COUNT = 3;
    // Technical ceiling for bounded in-memory lists, independent of balance configuration.
    private static final int MAX_CAPACITY = 1024;

    public GameRules {
        campaignGoals = List.copyOf(campaignGoals);
        initialSources = Set.copyOf(initialSources);
        Objects.requireNonNull(startingCoin);
        Objects.requireNonNull(startingSpecial);
        if (campaignGoals.size() != RoundKey.CAMPAIGN_STAGES) {
            throw new IllegalArgumentException("Campaign requires one goal per stage");
        }
        CoinAmount previous = CoinAmount.ZERO;
        for (var goal : campaignGoals) {
            if (goal.compareTo(previous) <= 0) {
                throw new IllegalArgumentException(
                        "Campaign goals must be positive and increasing");
            }
            previous = goal;
        }
        if (initialSources.size() != INITIAL_SOURCE_COUNT) {
            throw new IllegalArgumentException("Three distinct initial sources are required");
        }
        validateCapacity(inventoryCapacity);
        validateCapacity(shopSlots);
    }

    static void validateCapacity(int capacity) {
        if (capacity < 1 || capacity > MAX_CAPACITY) {
            throw new IllegalArgumentException("Capacity must be between 1 and " + MAX_CAPACITY);
        }
    }
}
