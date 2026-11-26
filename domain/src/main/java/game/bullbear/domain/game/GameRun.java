package game.bullbear.domain.game;

import static game.bullbear.domain.game.GamePhase.FINISHED;
import static game.bullbear.domain.game.GamePhase.GAME_OVER;
import static game.bullbear.domain.game.GamePhase.INVESTMENT_COMMITTED;
import static game.bullbear.domain.game.GamePhase.PLANNING;
import static game.bullbear.domain.game.GamePhase.POST_RESULT;
import static game.bullbear.domain.game.GamePhase.ROUND_ENTRY;
import static game.bullbear.domain.game.GamePhase.VICTORY;
import static game.bullbear.domain.game.GameRejection.Reason.DUPLICATE_CHARM;
import static game.bullbear.domain.game.GameRejection.Reason.INSUFFICIENT_COIN;
import static game.bullbear.domain.game.GameRejection.Reason.INVENTORY_FULL;
import static game.bullbear.domain.game.GameRejection.Reason.STALE_INPUT;
import static game.bullbear.domain.game.GameRejection.Reason.WRONG_PHASE;

import game.bullbear.domain.news.NewsSourceId;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Single-thread-owned aggregate. Only trusted coordinators supply economic results. */
public final class GameRun {
    private final GameRules rules;
    private final GameCatalog catalog;
    private final Map<CharmDefinition.InstanceId, CharmDefinition.Owned> charms =
            new LinkedHashMap<>();

    private RoundKey key;
    private GamePhase phase = ROUND_ENTRY;
    private CoinAmount goal;
    private CoinAmount wallet;
    private CoinAmount bank = CoinAmount.ZERO;
    private SpecialCurrencyAmount special;
    private Set<NewsSourceId> visibleSources;
    private Shop shop;

    // These references exist only in the corresponding round windows.
    private RoundEntry entry;
    private Investment.Commitment commitment;
    private GameSnapshot.Result result;

    private GameRun(RunId id, GameRules rules, GameCatalog catalog, Shop startingShop) {
        this.rules = Objects.requireNonNull(rules);
        this.catalog = Objects.requireNonNull(catalog);
        key = new RoundKey(id, 1, 1);
        goal = rules.campaignGoals().getFirst();
        wallet = rules.startingCoin();
        special = rules.startingSpecial();
        visibleSources = rules.initialSources();
        Shop.validateOffers(catalog, startingShop.slots(), rules.shopSlots());
        shop = startingShop;
    }

    public static GameRun start(RunId id, GameRules rules, GameCatalog catalog) {
        return new GameRun(id, rules, catalog, Shop.empty(rules.shopSlots()));
    }

    /** Start with a complete, explicitly supplied initial offer layout. */
    public static GameRun start(
            RunId id,
            GameRules rules,
            GameCatalog catalog,
            List<Optional<CharmDefinition.Id>> initialOffers) {
        return new GameRun(id, rules, catalog, new Shop(0, initialOffers));
    }

    public GameSnapshot snapshot() {
        var news = entry == null ? List.<NewsClaim>of() : entry.visibleClaims(visibleSources);
        return new GameSnapshot(
                key,
                phase,
                goal,
                wallet,
                bank,
                special,
                visibleSources,
                news,
                List.copyOf(charms.values()),
                shop,
                Optional.ofNullable(commitment),
                Optional.ofNullable(result));
    }

    public void enter(RoundEntry input) {
        require(input.key(), ROUND_ENTRY);
        CoinAmount credit = input.interest().plus(input.minimumIncome());
        if (key.stage() == 1 && key.round() == 1 && credit.units() != 0) {
            throw new IllegalArgumentException("Starting funds already cover the first round");
        }
        CoinAmount nextWallet = wallet.plus(credit);
        wallet = nextWallet;
        entry = input;
        phase = PLANNING;
    }

    /** Drafts are external immutable values. The next confirmation revalidates current funds. */
    public void deposit(RoundKey expected, CoinAmount amount) {
        requireStable(expected);
        if (amount.units() == 0) {
            throw new IllegalArgumentException("Deposit must be positive");
        }
        requireFunds(amount);
        CoinAmount nextWallet = wallet.minus(amount);
        CoinAmount nextBank = bank.plus(amount);
        wallet = nextWallet;
        bank = nextBank;
    }

    public void commit(RoundKey expected, Investment.Allocation allocation) {
        require(expected, PLANNING);
        var next = new Investment.Commitment(key, allocation);
        CoinAmount stake = allocation.total();
        requireFunds(stake);
        wallet = wallet.minus(stake);
        commitment = next;
        phase = INVESTMENT_COMMITTED;
    }

    public void settle(Investment.Settlement input) {
        require(input.commitment().key(), INVESTMENT_COMMITTED);
        if (!commitment.equals(input.commitment())) {
            throw new GameRejection(STALE_INPUT);
        }
        CoinAmount returned = input.totalReturned();
        CoinAmount nextWallet = wallet.plus(returned);
        SpecialCurrencyAmount nextSpecial = special.plus(input.reward());
        Shop nextShop =
                input.rotation().map(rotation -> shop.rotate(rotation, catalog)).orElse(shop);
        var nextResult =
                new GameSnapshot.Result(
                        commitment, input.movements(), returned, input.reward(), nextWallet);
        wallet = nextWallet;
        special = nextSpecial;
        shop = nextShop;
        result = nextResult;
        commitment = null;
        phase = POST_RESULT;
    }

    public void continueRound(RoundKey expected) {
        require(expected, POST_RESULT);
        if (key.finalRound()) {
            throw new GameRejection(WRONG_PHASE);
        }
        openEntry(key.nextRound(), goal);
    }

    /** Close the final deposit window and assess the cumulative bank balance in one transition. */
    public void closeStage(RoundKey expected, Optional<CoinAmount> nextEndlessGoal) {
        require(expected, POST_RESULT);
        if (!key.finalRound()) {
            throw new GameRejection(WRONG_PHASE);
        }
        Objects.requireNonNull(nextEndlessGoal);
        if (!key.endless() && nextEndlessGoal.isPresent()) {
            throw new IllegalArgumentException("Campaign goals come from the configured schedule");
        }
        if (bank.compareTo(goal) < 0) {
            phase = GAME_OVER;
        } else if (key.endless()) {
            advanceEndless(
                    nextEndlessGoal.orElseThrow(
                            () -> new IllegalArgumentException("Next endless goal is required")));
        } else if (key.stage() == RoundKey.CAMPAIGN_STAGES) {
            phase = VICTORY;
        } else {
            openEntry(key.nextStage(), rules.campaignGoals().get(key.stage()));
        }
    }

    public void chooseEndless(RoundKey expected, CoinAmount nextGoal) {
        require(expected, VICTORY);
        advanceEndless(nextGoal);
    }

    public void finish(RoundKey expected) {
        requireKey(expected);
        if (phase != VICTORY && !(key.endless() && phase == POST_RESULT)) {
            throw new GameRejection(WRONG_PHASE);
        }
        phase = FINISHED;
    }

    Optional<RoundEntry> currentEntry() {
        return Optional.ofNullable(entry);
    }

    /** Trusted structural grant for testing ownership; actual purchase and effects live later. */
    void acquire(RoundKey expected, CharmDefinition.Owned charm) {
        requireStable(expected);
        catalog.requireCharm(charm.definitionId());
        if (charms.containsKey(charm.instanceId())) {
            throw new GameRejection(DUPLICATE_CHARM);
        }
        if (charms.size() >= rules.inventoryCapacity()) {
            throw new GameRejection(INVENTORY_FULL);
        }
        charms.put(charm.instanceId(), charm);
    }

    void unlockSource(RoundKey expected, NewsSourceId source) {
        requireStable(expected);
        var next = EnumSet.copyOf(visibleSources);
        next.add(Objects.requireNonNull(source));
        visibleSources = next;
    }

    void consumeOffer(
            RoundKey expected, long revision, Shop.SlotId slot, CharmDefinition.Id offer) {
        requireStable(expected);
        shop = shop.consume(revision, slot, offer);
    }

    private void advanceEndless(CoinAmount nextGoal) {
        if (nextGoal.compareTo(goal) <= 0) {
            throw new IllegalArgumentException("Next goal must exceed the current goal");
        }
        openEntry(key.nextStage(), nextGoal);
    }

    private void openEntry(RoundKey nextKey, CoinAmount nextGoal) {
        key = nextKey;
        goal = nextGoal;
        entry = null;
        result = null;
        phase = ROUND_ENTRY;
    }

    private void require(RoundKey expected, GamePhase requiredPhase) {
        requireKey(expected);
        if (phase != requiredPhase) {
            throw new GameRejection(WRONG_PHASE);
        }
    }

    private void requireStable(RoundKey expected) {
        requireKey(expected);
        if (!phase.stableInteraction()) {
            throw new GameRejection(WRONG_PHASE);
        }
    }

    private void requireKey(RoundKey expected) {
        if (!key.equals(Objects.requireNonNull(expected))) {
            throw new GameRejection(STALE_INPUT);
        }
    }

    private void requireFunds(CoinAmount amount) {
        if (wallet.compareTo(amount) < 0) {
            throw new GameRejection(INSUFFICIENT_COIN);
        }
    }
}
