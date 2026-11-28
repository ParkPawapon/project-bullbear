package game.bullbear.application.market;

import game.bullbear.domain.game.RoundKey;
import game.bullbear.domain.game.RunId;
import game.bullbear.domain.market.MarketEngine.Resolution;
import game.bullbear.domain.market.MarketRegime;
import game.bullbear.domain.market.MarketRules;
import game.bullbear.domain.market.MarketTuning;
import game.bullbear.domain.stock.StockId;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Reproducible headless experiment. Run with the test, application and domain class directories.
 */
public final class MarketExperiment {
    private static final MarketRules DEFAULT = MarketTuning.provisional();
    private static final List<StockId> STOCKS = MarketRules.STOCKS;
    private static final List<MarketRegime> REGIMES = MarketRules.REGIMES;
    private static final RunId RUN = new RunId(1);
    private static final int CAMPAIGN_ROUNDS = RoundKey.CAMPAIGN_STAGES * RoundKey.ROUNDS_PER_STAGE;
    private static final int ISOLATED_SEEDS = 20_000;
    private static final int CAMPAIGN_SEEDS = 1000;

    private MarketExperiment() {}

    public static void main(String[] args) {
        if (Arrays.equals(args, new String[] {"--benchmark"})) {
            benchmark();
        } else if (args.length == 0) {
            statistics();
        } else {
            throw new IllegalArgumentException("Expected no arguments or --benchmark");
        }
    }

    private static void statistics() {
        Sample sideways = isolated(MarketRegime.SIDEWAYS);
        Sample high = isolated(MarketRegime.HIGH_VOLATILITY);
        Sample bull = isolated(MarketRegime.BULL);
        Sample bear = isolated(MarketRegime.BEAR);
        double[] ratio = new double[STOCKS.size()];
        boolean signs = true;
        boolean direction = true;
        boolean correlations = true;
        for (int i = 0; i < STOCKS.size(); i++) {
            Distribution neutral = sideways.returns[i];
            ratio[i] = high.returns[i].stddev() / neutral.stddev();
            signs &=
                    neutral.negative >= ISOLATED_SEEDS / 10
                            && neutral.positive >= ISOLATED_SEEDS / 10;
            direction &= bull.returns[i].mean() > bear.returns[i].mean();
            for (int j = i + 1; j < STOCKS.size(); j++) {
                double correlation = sideways.pairs[i][j].correlation();
                correlations &= correlation > 0 && correlation < 0.95;
            }
        }
        Arrays.sort(ratio);
        double wildcardRatio = sideways.returns[4].stddev() / sideways.returns[0].stddev();
        double volatilityRatio = ratio[ratio.length / 2];
        print(
                "reference signs=%s wildcard/stable=%.4f high/sideways-median=%.4f bull>bear=%s correlations=%s",
                signs, wildcardRatio, volatilityRatio, direction, correlations);
        natural();
    }

    private static Sample isolated(MarketRegime mood) {
        var settings = DEFAULT.settings();
        var opening =
                new MarketRules.Settings(
                        settings.initialIndexTicks(),
                        mood,
                        settings.commonAmplitudeBps(),
                        0,
                        0,
                        settings.newsCapBps(),
                        settings.minReturnBps(),
                        settings.maxReturnBps(),
                        settings.historyCapacity());
        var regimes =
                DEFAULT.regimes().values().stream()
                        .map(
                                r ->
                                        new MarketRules.Regime(
                                                r.id(),
                                                r.driftBps(),
                                                r.volatility(),
                                                0,
                                                r.transitions()))
                        .toList();
        var rules =
                new MarketRules(
                        "reference-" + mood.name(),
                        opening,
                        DEFAULT.profiles(),
                        regimes,
                        List.of());
        print(
                "isolated %s fingerprint=%s seeds=0..%d",
                mood, rules.fingerprint(), ISOLATED_SEEDS - 1);
        var sample = new Sample();
        for (long seed = 0; seed < ISOLATED_SEEDS; seed++) {
            var owner = new MarketSession(RUN, seed, rules);
            var prepared = owner.prepare(key(1));
            if (prepared.event().isPresent() || prepared.planning().mood() != mood) {
                throw new AssertionError("Invalid isolated sample");
            }
            sample.add(owner.resolve(prepared, List.of()), false);
        }
        sample.report();
        return sample;
    }

    private static void natural() {
        print(
                "natural fingerprint=%s seeds=0..%d rounds=%d",
                DEFAULT.fingerprint(), CAMPAIGN_SEEDS - 1, CAMPAIGN_ROUNDS);
        var sample = new Sample();
        int size = REGIMES.size();
        int[][] transitions = new int[size][size];
        int[] roundCount = new int[size];
        int[] eventCount = new int[size];
        int[] streakCount = new int[size];
        int[] longestStreak = new int[size];
        Set<Long> paths = new HashSet<>();
        for (long seed = 0; seed < CAMPAIGN_SEEDS; seed++) {
            var owner = new MarketSession(RUN, seed, DEFAULT);
            int previous = -1;
            int streak = 0;
            long path = 0;
            for (int ordinal = 1; ordinal <= CAMPAIGN_ROUNDS; ordinal++) {
                var prepared = owner.prepare(key(ordinal));
                int mood = REGIMES.indexOf(prepared.planning().mood());
                if (previous >= 0) {
                    transitions[previous][mood]++;
                    if (mood != previous) {
                        streakCount[previous]++;
                        longestStreak[previous] = Math.max(longestStreak[previous], streak);
                        streak = 0;
                    }
                }
                streak++;
                previous = mood;
                roundCount[mood]++;
                boolean event = prepared.event().isPresent();
                if (event) {
                    eventCount[mood]++;
                }
                var proposal = owner.resolve(prepared, List.of());
                sample.add(proposal, event);
                var accepted = owner.accept(proposal);
                if (accepted.sequence() != ordinal || accepted.history().size() != ordinal) {
                    throw new AssertionError("Campaign history is misaligned");
                }
                for (var move : proposal.proposedRound().moves().values()) {
                    path = 31 * path + move.returnBps();
                }
            }
            streakCount[previous]++;
            longestStreak[previous] = Math.max(longestStreak[previous], streak);
            paths.add(path);
        }
        sample.report();
        for (int i = 0; i < size; i++) {
            var row = DEFAULT.regimes().get(REGIMES.get(i));
            int prior = Arrays.stream(transitions[i]).sum();
            boolean eventsMatch = close(eventCount[i], roundCount[i], row.eventChanceBps());
            boolean rowMatches = true;
            for (int j = 0; j < size; j++) {
                rowMatches &=
                        close(transitions[i][j], prior, row.transitions().get(REGIMES.get(j)));
            }
            print(
                    "%s rounds=%d events=%d/%.4f expected=%d bps within=%s transitions=%s within=%s streaks=%d mean=%.3f max=%d",
                    row.id(),
                    roundCount[i],
                    eventCount[i],
                    (double) eventCount[i] / roundCount[i],
                    row.eventChanceBps(),
                    eventsMatch,
                    Arrays.toString(transitions[i]),
                    rowMatches,
                    streakCount[i],
                    (double) roundCount[i] / streakCount[i],
                    longestStreak[i]);
        }
        print("unique return sequence signatures=%d/%d", paths.size(), CAMPAIGN_SEEDS);
    }

    private static boolean close(int matches, int samples, int expectedBps) {
        double expected = expectedBps / 10000.0;
        double tolerance = Math.max(0.01, 5 * Math.sqrt(expected * (1 - expected) / samples));
        return Math.abs((double) matches / samples - expected) <= tolerance;
    }

    private static void benchmark() {
        int warmup = 2000;
        long[] samples = new long[10_000];
        long checksum = 0;
        var owner = new MarketSession(RUN, 0, DEFAULT);
        for (int sample = 0; sample < warmup + samples.length; sample++) {
            int ordinal = sample % CAMPAIGN_ROUNDS + 1;
            if (sample > 0 && ordinal == 1) {
                owner = new MarketSession(RUN, sample / CAMPAIGN_ROUNDS, DEFAULT);
            }
            var round = key(ordinal);
            long start = System.nanoTime();
            var prepared = owner.prepare(round);
            var proposal = owner.resolve(prepared, List.of());
            var accepted = owner.accept(proposal);
            long elapsed = System.nanoTime() - start;
            checksum += accepted.quotes().get(StockId.STABLE).indexTicks();
            if (sample >= warmup) {
                samples[sample - warmup] = elapsed;
            }
        }
        double seconds = Arrays.stream(samples).sum() / 1e9;
        Arrays.sort(samples);
        print(
                "prepare+resolve+accept campaign rounds warmup=%d samples=%d summed=%.6f s rounds/s=%.0f p50=%.4f ms p95=%.4f ms checksum=%d",
                warmup,
                samples.length,
                seconds,
                samples.length / seconds,
                samples[samples.length / 2 - 1] / 1e6,
                samples[samples.length * 95 / 100 - 1] / 1e6,
                checksum);
    }

    private static RoundKey key(int ordinal) {
        int roundsPerStage = RoundKey.ROUNDS_PER_STAGE;
        return new RoundKey(
                RUN, (ordinal - 1) / roundsPerStage + 1, (ordinal - 1) % roundsPerStage + 1);
    }

    private static void print(String format, Object... arguments) {
        System.out.printf(Locale.ROOT, format + "%n", arguments);
    }

    private static final class Sample {
        private final Distribution[] returns = new Distribution[STOCKS.size()];
        private final Distribution[] eventful = new Distribution[STOCKS.size()];
        private final Distribution[] uneventful = new Distribution[STOCKS.size()];
        private final Correlation[] common = new Correlation[STOCKS.size()];
        private final Correlation[][] pairs = new Correlation[STOCKS.size()][STOCKS.size()];
        private long minimumIndex = Long.MAX_VALUE;
        private long maximumIndex;

        Sample() {
            for (int i = 0; i < STOCKS.size(); i++) {
                returns[i] = new Distribution();
                eventful[i] = new Distribution();
                uneventful[i] = new Distribution();
                common[i] = new Correlation();
                for (int j = i + 1; j < STOCKS.size(); j++) {
                    pairs[i][j] = new Correlation();
                }
            }
        }

        void add(Resolution proposal, boolean event) {
            for (int i = 0; i < STOCKS.size(); i++) {
                StockId stock = STOCKS.get(i);
                var parts = proposal.attribution().get(stock);
                var move = proposal.proposedRound().moves().get(stock);
                int result = move.returnBps();
                long index = move.afterTicks();
                if (parts.effectiveBps() != result
                        || parts.rawBps() + parts.clampAdjustmentBps() != result
                        || index < 10_000L
                        || index > 1_000_000_000_000_000_000L) {
                    throw new AssertionError("Market bounds or attribution failed");
                }
                minimumIndex = Math.min(minimumIndex, index);
                maximumIndex = Math.max(maximumIndex, index);
                returns[i].add(result);
                (event ? eventful : uneventful)[i].add(result);
                common[i].add(result, parts.commonBps());
                for (int j = i + 1; j < STOCKS.size(); j++) {
                    pairs[i][j].add(
                            result,
                            proposal.proposedRound().moves().get(STOCKS.get(j)).returnBps());
                }
            }
        }

        void report() {
            for (int i = 0; i < STOCKS.size(); i++) {
                Distribution d = returns[i];
                print(
                        "%s n=%d min=%d max=%d mean=%.2f sd=%.2f signs(+/-/0)=%d/%d/%d p05/50/95=%d/%d/%d caps(low/high)=%d/%d common=%s",
                        STOCKS.get(i),
                        d.count,
                        d.min,
                        d.max,
                        d.mean(),
                        d.stddev(),
                        d.positive,
                        d.negative,
                        d.count - d.positive - d.negative,
                        d.percentile(5),
                        d.percentile(50),
                        d.percentile(95),
                        d.histogram[0],
                        d.histogram[d.histogram.length - 1],
                        common[i].display());
                if (eventful[i].count > 0) {
                    print(
                            "  event n=%d mean=%.2f sd=%.2f p05/50/95=%d/%d/%d; no event n=%d mean=%.2f sd=%.2f p05/50/95=%d/%d/%d",
                            eventful[i].count,
                            eventful[i].mean(),
                            eventful[i].stddev(),
                            eventful[i].percentile(5),
                            eventful[i].percentile(50),
                            eventful[i].percentile(95),
                            uneventful[i].count,
                            uneventful[i].mean(),
                            uneventful[i].stddev(),
                            uneventful[i].percentile(5),
                            uneventful[i].percentile(50),
                            uneventful[i].percentile(95));
                }
                for (int j = i + 1; j < STOCKS.size(); j++) {
                    print("  corr(%s,%s)=%s", STOCKS.get(i), STOCKS.get(j), pairs[i][j].display());
                }
            }
            print("index ticks min=%d max=%d violations=0", minimumIndex, maximumIndex);
        }
    }

    private static final class Distribution {
        private static final int LOWER = -5000;
        private static final int UPPER = 6000;
        private final int[] histogram = new int[UPPER - LOWER + 1];
        private int count;
        private int positive;
        private int negative;
        private int min = Integer.MAX_VALUE;
        private int max = Integer.MIN_VALUE;
        private long sum;
        private long squares;

        void add(int value) {
            if (value < LOWER || value > UPPER) {
                throw new AssertionError("Return outside configured bounds");
            }
            histogram[value - LOWER]++;
            count++;
            positive += value > 0 ? 1 : 0;
            negative += value < 0 ? 1 : 0;
            min = Math.min(min, value);
            max = Math.max(max, value);
            sum += value;
            squares += (long) value * value;
        }

        double mean() {
            return (double) sum / count;
        }

        double stddev() {
            return Math.sqrt(((long) count * squares - sum * sum) / ((double) count * count));
        }

        int percentile(int percent) {
            int rank = (count * percent + 99) / 100;
            int total = 0;
            for (int i = 0; i < histogram.length; i++) {
                total += histogram[i];
                if (total >= rank) {
                    return LOWER + i;
                }
            }
            throw new AssertionError("Percentile is outside the sample");
        }
    }

    private static final class Correlation {
        private long count;
        private long sumX;
        private long sumY;
        private long sumXX;
        private long sumYY;
        private long sumXY;

        void add(int x, int y) {
            count++;
            sumX += x;
            sumY += y;
            sumXX += (long) x * x;
            sumYY += (long) y * y;
            sumXY += (long) x * y;
        }

        double correlation() {
            double spreadX = count * sumXX - sumX * sumX;
            double spreadY = count * sumYY - sumY * sumY;
            return spreadX == 0 || spreadY == 0
                    ? Double.NaN
                    : (count * sumXY - sumX * sumY) / Math.sqrt(spreadX * spreadY);
        }

        String display() {
            double value = correlation();
            return Double.isFinite(value) ? String.format(Locale.ROOT, "%.4f", value) : "N/A";
        }
    }
}
