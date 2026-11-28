package game.bullbear.domain.market;

import game.bullbear.domain.stock.StockId;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

/** Immutable configuration. Explicit canonical orders are part of the replay format. */
public final class MarketRules {
    public static final List<StockId> STOCKS =
            List.of(
                    StockId.STABLE,
                    StockId.GROWTH,
                    StockId.VOLATILE,
                    StockId.FOLLOWER,
                    StockId.WILDCARD);
    public static final List<MarketRegime> REGIMES =
            List.of(
                    MarketRegime.BULL,
                    MarketRegime.BEAR,
                    MarketRegime.SIDEWAYS,
                    MarketRegime.HIGH_VOLATILITY,
                    MarketRegime.CRASH,
                    MarketRegime.RECOVERY);
    private static final int MAX_EVENTS = 64;
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z0-9][A-Za-z0-9_.-]{0,63}");

    public record Settings(
            long initialIndexTicks,
            MarketRegime initialRegime,
            int commonAmplitudeBps,
            int momentumCapBps,
            int eventCapBps,
            int newsCapBps,
            int minReturnBps,
            int maxReturnBps,
            int historyCapacity) {
        public Settings {
            Objects.requireNonNull(initialRegime);
            MarketMath.requireRange(
                    initialIndexTicks, MarketMath.MIN_INDEX, MarketMath.MAX_INDEX, "Initial index");
            MarketMath.requireRange(commonAmplitudeBps, 0, 5000, "Common noise amplitude");
            MarketMath.requireRange(momentumCapBps, 0, MarketMath.BASIS, "Momentum cap");
            MarketMath.requireRange(eventCapBps, 0, MarketMath.BASIS, "Event cap");
            MarketMath.requireRange(newsCapBps, 0, MarketMath.BASIS, "News cap");
            MarketMath.requireRange(minReturnBps, -9999, -1, "Minimum return");
            MarketMath.requireRange(maxReturnBps, 1, MarketMath.BASIS, "Maximum return");
            MarketMath.requireRange(historyCapacity, 1, 512, "History capacity");
        }
    }

    public record Regime(
            MarketRegime id,
            int driftBps,
            int volatility,
            int eventChanceBps,
            Map<MarketRegime, Integer> transitions) {
        public Regime {
            Objects.requireNonNull(id);
            MarketMath.requireRange(driftBps, -5000, 5000, "Regime drift");
            MarketMath.requireRange(volatility, 1, 2500, "Regime volatility");
            MarketMath.requireRange(eventChanceBps, 0, MarketMath.BASIS, "Event chance");
            transitions = orderedMap(REGIMES, transitions);
            int total = 0;
            for (int weight : transitions.values()) {
                MarketMath.requireRange(weight, 0, MarketMath.BASIS, "Transition weight");
                total += weight;
            }
            if (total != MarketMath.BASIS) {
                throw new IllegalArgumentException("Transition weights must total 10000");
            }
        }
    }

    public enum EventCategory {
        MACRO,
        SECTOR,
        SENTIMENT,
        CROSS_SECTOR,
        BROAD_MARKET
    }

    public record Event(
            String id, EventCategory category, Set<StockId> targets, int shockBps, int weight) {
        public Event {
            requireIdentifier(id);
            Objects.requireNonNull(category);
            MarketMath.requireRange(shockBps, -3000, 3000, "Event shock");
            MarketMath.requireRange(weight, 1, MarketMath.BASIS, "Event weight");
            targets = Set.copyOf(targets);
            if (targets.isEmpty() || !STOCKS.containsAll(targets)) {
                throw new IllegalArgumentException("Event must target known stocks");
            }
            targets = Collections.unmodifiableSet(EnumSet.copyOf(targets));
        }
    }

    private final String version;
    private final Settings settings;
    private final Map<StockId, StockProfile> profiles;
    private final Map<MarketRegime, Regime> regimes;
    private final List<Event> events;
    private final int eventWeight;
    private final String fingerprint;

    public MarketRules(
            String version,
            Settings settings,
            Map<StockId, StockProfile> profiles,
            List<Regime> regimes,
            List<Event> events) {
        requireIdentifier(version);
        this.version = version;
        this.settings = Objects.requireNonNull(settings);
        this.profiles = orderedMap(STOCKS, profiles);
        if (regimes.size() != REGIMES.size()) {
            throw new IllegalArgumentException("Regimes must cover all market moods");
        }
        Map<MarketRegime, Regime> byId = new LinkedHashMap<>();
        for (Regime regime : regimes) {
            if (byId.putIfAbsent(regime.id(), regime) != null) {
                throw new IllegalArgumentException("Duplicate regime");
            }
        }
        this.regimes = orderedMap(REGIMES, byId);
        if (events.size() > MAX_EVENTS) {
            throw new IllegalArgumentException("Event catalog exceeds its capacity");
        }
        this.events = events.stream().sorted(Comparator.comparing(Event::id)).toList();
        if (this.events.isEmpty()
                && this.regimes.values().stream().anyMatch(r -> r.eventChanceBps() > 0)) {
            throw new IllegalArgumentException("A positive event chance requires a catalog");
        }
        if (this.events.stream().map(Event::id).distinct().count() != this.events.size()) {
            throw new IllegalArgumentException("Duplicate event ID");
        }
        this.eventWeight = this.events.stream().mapToInt(Event::weight).sum();
        this.fingerprint =
                HexFormat.of()
                        .formatHex(
                                sha256().digest(canonicalText().getBytes(StandardCharsets.UTF_8)));
    }

    public String version() {
        return version;
    }

    public Settings settings() {
        return settings;
    }

    public Map<StockId, StockProfile> profiles() {
        return profiles;
    }

    public Map<MarketRegime, Regime> regimes() {
        return regimes;
    }

    public List<Event> events() {
        return events;
    }

    public String fingerprint() {
        return fingerprint;
    }

    int eventWeight() {
        return eventWeight;
    }

    static void requireIdentifier(String value) {
        if (!IDENTIFIER.matcher(Objects.requireNonNull(value)).matches()) {
            throw new IllegalArgumentException("Invalid market identifier");
        }
    }

    static <K, V> Map<K, V> orderedMap(List<K> ids, Map<K, V> values) {
        if (!values.keySet().equals(Set.copyOf(ids))) {
            throw new IllegalArgumentException("Market map must have exactly the expected keys");
        }
        return marketMap(ids, id -> Objects.requireNonNull(values.get(id)));
    }

    static <K, V> Map<K, V> marketMap(List<K> ids, Function<K, V> value) {
        Map<K, V> ordered = new LinkedHashMap<>();
        for (K id : ids) {
            ordered.put(id, value.apply(id));
        }
        return Collections.unmodifiableMap(ordered);
    }

    private String canonicalText() {
        StringBuilder text = new StringBuilder();
        appendRow(text, "market-rules-v1", version);
        appendRow(
                text,
                settings.initialIndexTicks(),
                settings.initialRegime(),
                settings.commonAmplitudeBps(),
                settings.momentumCapBps(),
                settings.eventCapBps(),
                settings.newsCapBps(),
                settings.minReturnBps(),
                settings.maxReturnBps(),
                settings.historyCapacity());
        profiles.forEach(
                (id, p) ->
                        appendRow(
                                text,
                                id,
                                p.ownAmplitudeBps(),
                                p.commonBeta(),
                                p.moodBeta(),
                                p.eventSensitivity(),
                                p.momentumWeight()));
        regimes.forEach(
                (id, r) -> {
                    appendRow(text, id, r.driftBps(), r.volatility(), r.eventChanceBps());
                    appendRow(text, r.transitions().values().toArray());
                });
        for (Event e : events) {
            appendRow(text, e.id(), e.category(), e.shockBps(), e.weight());
            appendRow(text, STOCKS.stream().filter(e.targets()::contains).toArray());
        }
        return text.toString();
    }

    private static void appendRow(StringBuilder text, Object... values) {
        for (Object value : values) {
            text.append(value).append('|');
        }
        text.append('\n');
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("JDK is missing SHA-256", e);
        }
    }
}
