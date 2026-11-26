package game.bullbear.domain.game;

import game.bullbear.domain.news.NewsSourceId;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Trusted entry data. A claim states a direction with truth and resolution deferred. */
public record RoundEntry(
        RoundKey key, CoinAmount interest, CoinAmount minimumIncome, List<NewsClaim> claims) {
    public RoundEntry {
        Objects.requireNonNull(key);
        Objects.requireNonNull(interest);
        Objects.requireNonNull(minimumIncome);
        claims = List.copyOf(claims);
        var ids = new HashSet<NewsClaim.Id>();
        for (var claim : claims) {
            if (!ids.add(claim.id())) {
                throw new IllegalArgumentException("Duplicate news claim ID in round");
            }
        }
    }

    List<NewsClaim> visibleClaims(Set<NewsSourceId> sources) {
        return claims.stream().filter(claim -> sources.contains(claim.source())).toList();
    }

    @Override
    public String toString() {
        return "RoundEntry[" + key + "]";
    }
}
