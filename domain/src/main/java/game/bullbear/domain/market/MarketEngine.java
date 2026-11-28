package game.bullbear.domain.market;

import game.bullbear.domain.game.RoundKey;
import game.bullbear.domain.game.RunId;
import game.bullbear.domain.market.MarketDraws.Channel;
import game.bullbear.domain.market.MarketImpact.EventRef;
import game.bullbear.domain.market.MarketSnapshot.HistoryRound;
import game.bullbear.domain.market.MarketSnapshot.PriceMove;
import game.bullbear.domain.market.MarketSnapshot.Quote;
import game.bullbear.domain.stock.StockId;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.ToIntFunction;

/** Pure preparation and resolution. The application owns promotion of accepted state. */
public final class MarketEngine {
    private static final int MAX_IMPACTS = 64;
    private final MarketRules rules;

    public record PlanningView(RoundKey key, MarketRegime mood, MarketSnapshot accepted) {}

    public record EventOccurrence(EventRef reference, MarketRules.Event definition) {}

    public record Attribution(
            int moodBps,
            int commonBps,
            int ownBps,
            int momentumBps,
            int eventBps,
            int newsBps,
            int rawBps,
            int clampAdjustmentBps,
            int effectiveBps) {}

    /** Trusted context. The planning projection is the safe view for a future player interface. */
    public static final class PreparedRound {
        private final MarketState previous;
        private final PlanningView planning;
        private final int sharedNoise;
        private final Map<StockId, Integer> ownNoise;
        private final Optional<EventOccurrence> event;

        private PreparedRound(
                MarketState previous,
                PlanningView planning,
                int sharedNoise,
                Map<StockId, Integer> ownNoise,
                Optional<EventOccurrence> event) {
            this.previous = previous;
            this.planning = planning;
            this.sharedNoise = sharedNoise;
            this.ownNoise = ownNoise;
            this.event = event;
        }

        public PlanningView planning() {
            return planning;
        }

        public Optional<EventOccurrence> event() {
            return event;
        }
    }

    /** Trusted proposal. Creation and inspection have no effect on accepted state. */
    public static final class Resolution {
        private final MarketState previous;
        private final MarketState next;
        private final Map<StockId, Attribution> attribution;

        private Resolution(
                MarketState previous, MarketState next, Map<StockId, Attribution> attribution) {
            this.previous = previous;
            this.next = next;
            this.attribution = attribution;
        }

        public Map<StockId, Attribution> attribution() {
            return attribution;
        }

        public HistoryRound proposedRound() {
            return next.snapshot().history().getLast();
        }
    }

    public MarketEngine(MarketRules rules) {
        this.rules = Objects.requireNonNull(rules);
    }

    public MarketState initial(RunId runId) {
        Quote openingQuote = new Quote(rules.settings().initialIndexTicks(), OptionalInt.empty());
        return new MarketState(
                rules.fingerprint(),
                new MarketSnapshot(
                        runId,
                        0,
                        rules.settings().initialRegime(),
                        MarketRules.marketMap(MarketRules.STOCKS, stock -> openingQuote),
                        List.of()));
    }

    public PreparedRound prepare(MarketState state, RoundKey key, MarketDraws draws) {
        requireRules(state);
        Objects.requireNonNull(key);
        Objects.requireNonNull(draws);
        MarketSnapshot accepted = state.snapshot();
        long ordinal = (key.stage() - 1L) * RoundKey.ROUNDS_PER_STAGE + key.round();
        if (!accepted.runId().equals(key.runId())
                || ordinal != Math.incrementExact(accepted.sequence())) {
            throw new IllegalArgumentException("Round must immediately follow this market state");
        }

        MarketRegime mood = accepted.regime();
        if (accepted.sequence() != 0) {
            Map<MarketRegime, Integer> row = rules.regimes().get(mood).transitions();
            int choice =
                    draw(
                            draws,
                            ordinal,
                            Channel.REGIME_TRANSITION,
                            mood.name(),
                            0,
                            MarketMath.BASIS);
            mood = weightedChoice(MarketRules.REGIMES, row::get, choice);
        }
        MarketRules.Regime regime = rules.regimes().get(mood);
        int sharedNoise =
                MarketMath.scale(
                        noise(
                                draws,
                                ordinal,
                                Channel.COMMON_NOISE,
                                "",
                                rules.settings().commonAmplitudeBps()),
                        regime.volatility());
        Map<StockId, Integer> ownNoise =
                MarketRules.marketMap(
                        MarketRules.STOCKS,
                        stock ->
                                MarketMath.scale(
                                        noise(
                                                draws,
                                                ordinal,
                                                Channel.STOCK_NOISE,
                                                stock.name(),
                                                rules.profiles().get(stock).ownAmplitudeBps()),
                                        regime.volatility()));
        return new PreparedRound(
                state,
                new PlanningView(key, mood, accepted),
                sharedNoise,
                ownNoise,
                prepareEvent(key, draws, ordinal, regime.eventChanceBps()));
    }

    private Optional<EventOccurrence> prepareEvent(
            RoundKey key, MarketDraws draws, long ordinal, int chance) {
        int occurrence = draw(draws, ordinal, Channel.EVENT_OCCURRENCE, "", 0, MarketMath.BASIS);
        if (occurrence >= chance) {
            return Optional.empty();
        }
        int selection = draw(draws, ordinal, Channel.EVENT_SELECTION, "", 0, rules.eventWeight());
        MarketRules.Event event =
                weightedChoice(rules.events(), MarketRules.Event::weight, selection);
        return Optional.of(new EventOccurrence(new EventRef(key, event.id()), event));
    }

    public Resolution resolve(PreparedRound prepared, List<MarketImpact> impacts) {
        requireRules(prepared.previous);
        MarketSnapshot accepted = prepared.planning.accepted();
        Map<StockId, Long> news = totalImpacts(prepared.event, impacts);
        Map<StockId, Attribution> attribution =
                MarketRules.marketMap(
                        MarketRules.STOCKS,
                        stock ->
                                attribute(
                                        prepared,
                                        stock,
                                        accepted.quotes().get(stock),
                                        news.getOrDefault(stock, 0L)));

        Map<StockId, PriceMove> moves =
                MarketRules.marketMap(
                        MarketRules.STOCKS,
                        stock -> {
                            long before = accepted.quotes().get(stock).indexTicks();
                            int effective = attribution.get(stock).effectiveBps();
                            return new PriceMove(
                                    before, MarketMath.advanceIndex(before, effective), effective);
                        });
        Map<StockId, Quote> quotes =
                MarketRules.marketMap(
                        MarketRules.STOCKS,
                        stock -> {
                            PriceMove move = moves.get(stock);
                            return new Quote(move.afterTicks(), OptionalInt.of(move.returnBps()));
                        });

        int capacity = rules.settings().historyCapacity();
        List<HistoryRound> history =
                new ArrayList<>(Math.min(capacity, accepted.history().size() + 1));
        int start = Math.max(0, accepted.history().size() - capacity + 1);
        history.addAll(accepted.history().subList(start, accepted.history().size()));
        history.add(new HistoryRound(prepared.planning.key(), prepared.planning.mood(), moves));
        MarketSnapshot next =
                new MarketSnapshot(
                        accepted.runId(),
                        Math.incrementExact(accepted.sequence()),
                        prepared.planning.mood(),
                        quotes,
                        history);
        return new Resolution(
                prepared.previous, new MarketState(rules.fingerprint(), next), attribution);
    }

    public MarketState accept(MarketState current, Resolution proposal) {
        requireRules(current);
        if (proposal.previous != current) {
            throw new IllegalArgumentException(
                    "Market proposal does not match the current predecessor");
        }
        return proposal.next;
    }

    private Attribution attribute(
            PreparedRound prepared, StockId stock, Quote previous, long newsTotal) {
        MarketRules.Settings settings = rules.settings();
        StockProfile profile = rules.profiles().get(stock);
        MarketRules.Regime regime = rules.regimes().get(prepared.planning.mood());
        int mood = MarketMath.scale(regime.driftBps(), profile.moodBeta());
        int common = MarketMath.scale(prepared.sharedNoise, profile.commonBeta());
        int own = prepared.ownNoise.get(stock);
        int momentum =
                symmetricCap(
                        MarketMath.scale(
                                previous.lastReturnBps().orElse(0), profile.momentumWeight()),
                        settings.momentumCapBps());
        int shock =
                prepared.event
                        .map(EventOccurrence::definition)
                        .filter(event -> event.targets().contains(stock))
                        .map(MarketRules.Event::shockBps)
                        .orElse(0);
        int event =
                symmetricCap(
                        MarketMath.scale(shock, profile.eventSensitivity()),
                        settings.eventCapBps());
        int news = symmetricCap(newsTotal, settings.newsCapBps());
        int raw = Math.toIntExact((long) mood + common + own + momentum + event + news);
        int effective = MarketMath.clamp(raw, settings.minReturnBps(), settings.maxReturnBps());
        return new Attribution(
                mood, common, own, momentum, event, news, raw, effective - raw, effective);
    }

    private static Map<StockId, Long> totalImpacts(
            Optional<EventOccurrence> event, List<MarketImpact> impacts) {
        if (impacts.size() > MAX_IMPACTS) {
            throw new IllegalArgumentException("Too many market impacts");
        }
        Set<String> ids = new HashSet<>();
        Map<StockId, Long> totals = new EnumMap<>(StockId.class);
        Optional<EventRef> expected = event.map(EventOccurrence::reference);
        for (MarketImpact impact : impacts) {
            if (!ids.add(impact.id())) {
                throw new IllegalArgumentException("Duplicate market impact ID");
            }
            if (impact.cause().isPresent() && !impact.cause().equals(expected)) {
                throw new IllegalArgumentException(
                        "Impact references a different market occurrence");
            }
            totals.merge(impact.target(), (long) impact.bps(), Math::addExact);
        }
        return totals;
    }

    private void requireRules(MarketState state) {
        if (!rules.fingerprint().equals(state.rulesFingerprint())) {
            throw new IllegalArgumentException("Market state uses different rules");
        }
    }

    private static int symmetricCap(long value, int cap) {
        return MarketMath.clamp(value, -cap, cap);
    }

    private static int noise(
            MarketDraws draws, long ordinal, Channel channel, String subject, int amplitude) {
        if (amplitude == 0) {
            return 0;
        }
        int bound = 2 * amplitude + 1;
        int first = draw(draws, ordinal, channel, subject, 0, bound) - amplitude;
        int second = draw(draws, ordinal, channel, subject, 1, bound) - amplitude;
        return Math.toIntExact(MarketMath.roundRatio((long) first + second, 2));
    }

    private static int draw(
            MarketDraws draws,
            long ordinal,
            Channel channel,
            String subject,
            int index,
            int bound) {
        int result = draws.draw(ordinal, channel, subject, index, bound);
        MarketMath.requireRange(result, 0, bound - 1L, "Market draw");
        return result;
    }

    private static <T> T weightedChoice(List<T> choices, ToIntFunction<T> weight, int draw) {
        int cumulative = 0;
        for (T option : choices) {
            cumulative += weight.applyAsInt(option);
            if (draw < cumulative) {
                return option;
            }
        }
        throw new IllegalStateException("Market weights do not cover the draw");
    }
}
