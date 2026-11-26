package game.bullbear.domain.game;

import static game.bullbear.domain.game.GameFixtures.*;
import static game.bullbear.domain.game.GamePhase.*;
import static game.bullbear.domain.game.GameRejection.Reason.*;
import static org.junit.jupiter.api.Assertions.*;

import game.bullbear.domain.news.NewsSourceId;
import game.bullbear.domain.stock.StockId;
import java.math.BigInteger;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class GameRunTest {
    @Test
    void entryCreditsOnlyTheTargetRoundAndUsesFreshWallet() {
        var run = run(11, 2);
        var start = run.snapshot();
        assertEquals(ROUND_ENTRY, start.phase());
        assertEquals(coin(11), start.wallet());
        assertEquals(coin(0), start.bank());
        assertEquals(special(2), start.special());
        assertEquals(SOURCES, start.visibleSources());
        assertTrue(start.result().isEmpty());
        assertTrue(start.commitment().isEmpty());
        assertTrue(run.currentEntry().isEmpty());
        assertThrows(
                NullPointerException.class, () -> GameRun.start(new RunId(1), null, catalog()));
        assertThrows(
                NullPointerException.class, () -> GameRun.start(new RunId(1), rules(1, 0), null));
        rejected(run, WRONG_PHASE, () -> run.deposit(start.key(), coin(1)));
        unchanged(run, IllegalArgumentException.class, () -> run.enter(entry(start.key(), 1, 0)));
        unchanged(
                run,
                ArithmeticException.class,
                () -> run.enter(entry(start.key(), Long.MAX_VALUE, 1)));
        run.enter(entry(start.key(), 0, 0));
        rejected(run, WRONG_PHASE, () -> run.enter(entry(start.key(), 0, 0)));
        run.deposit(start.key(), coin(2));
        resolve(run, 1, 1);
        run.continueRound(start.key());
        var second = key(run);
        rejected(run, STALE_INPUT, () -> run.enter(entry(start.key(), 10, 10)));
        run.enter(entry(second, 2, 3));
        assertEquals(coin(14), run.snapshot().wallet());
        assertEquals(coin(2), run.snapshot().bank());
        assertEquals(special(2), run.snapshot().special());
        rejected(run, WRONG_PHASE, () -> run.enter(entry(second, 2, 3)));
    }

    @Test
    void wholeCoinDepositionConservesFundsAndDoesNotReserveDrafts() {
        var run = planning(25, 4);
        var draft = allocate(23);
        var first = run.snapshot();
        run.deposit(first.key(), coin(5));
        var after = run.snapshot();
        assertEquals(PLANNING, after.phase());
        assertEquals(first.key(), after.key());
        assertEquals(first.shop(), after.shop());
        assertEquals(coin(20), after.wallet());
        assertEquals(coin(5), after.bank());
        assertEquals(first.special(), after.special());
        assertEquals(
                BigInteger.valueOf(first.wallet().units())
                        .add(BigInteger.valueOf(first.bank().units())),
                BigInteger.valueOf(after.wallet().units())
                        .add(BigInteger.valueOf(after.bank().units())));
        rejected(run, INSUFFICIENT_COIN, () -> run.commit(first.key(), draft));
        rejected(run, INSUFFICIENT_COIN, () -> run.deposit(first.key(), coin(21)));
        unchanged(run, IllegalArgumentException.class, () -> run.deposit(first.key(), coin(0)));
        unchanged(run, NullPointerException.class, () -> run.deposit(first.key(), null));
        run.deposit(first.key(), coin(20));
        assertEquals(CoinAmount.ZERO, run.snapshot().wallet());
        assertEquals(coin(25), run.snapshot().bank());
    }

    @Test
    void depositRejectsBankOverflowBeforeDebitingWallet() {
        var run = planning(Long.MAX_VALUE, 0);
        var first = key(run);
        run.deposit(first, coin(Long.MAX_VALUE - 1));
        resolve(run, 1, Long.MAX_VALUE);
        unchanged(run, ArithmeticException.class, () -> run.deposit(first, coin(2)));
        assertEquals(coin(Long.MAX_VALUE - 1), run.snapshot().bank());
    }

    @Test
    void confirmsAtCurrentAffordabilityAndFreezesStableInteractions() {
        var run = planning(5, 4);
        var first = key(run);
        unchanged(run, IllegalArgumentException.class, () -> run.commit(first, allocate(0)));
        rejected(run, INSUFFICIENT_COIN, () -> run.commit(first, allocate(6)));
        rejected(run, WRONG_PHASE, () -> run.continueRound(first));
        var amounts = new EnumMap<StockId, CoinAmount>(StockId.class);
        for (var stock : StockId.values()) {
            amounts.put(stock, coin(1));
        }
        run.commit(first, new Investment.Allocation(amounts));
        amounts.clear();
        var locked = run.snapshot();
        assertEquals(5, locked.commitment().orElseThrow().allocation().amounts().size());
        assertEquals(coin(5), locked.commitment().orElseThrow().allocation().total());
        assertEquals(CoinAmount.ZERO, locked.wallet());
        assertEquals(INVESTMENT_COMMITTED, locked.phase());
        rejected(run, WRONG_PHASE, () -> run.deposit(first, coin(1)));
        rejected(run, WRONG_PHASE, () -> run.commit(first, allocate(1)));
        rejected(run, WRONG_PHASE, () -> run.continueRound(first));
        rejected(run, WRONG_PHASE, () -> run.closeStage(first, Optional.empty()));
        rejected(run, WRONG_PHASE, () -> run.consumeOffer(first, 0, new Shop.SlotId(1), CHARM));
        rejected(run, WRONG_PHASE, () -> run.unlockSource(first, NewsSourceId.D));
        rejected(run, WRONG_PHASE, () -> run.acquire(first, owned(1)));
    }

    @Test
    void settlementCreditsProfitsOnceAndLeavesImmutableResult() {
        var run = planning(12, 5);
        var first = key(run);
        run.deposit(first, coin(1));
        run.commit(first, allocate(3));
        var input = settlement(run, 5, 2);
        run.settle(input);
        var settled = run.snapshot();
        var result = settled.result().orElseThrow();
        assertEquals(POST_RESULT, settled.phase());
        assertTrue(settled.commitment().isEmpty());
        assertEquals(coin(13), settled.wallet());
        assertEquals(coin(13), result.walletAfter());
        assertEquals(coin(1), settled.bank());
        assertEquals(special(7), settled.special());
        assertEquals(coin(5), result.returned());
        assertEquals(2L, result.profitOrLoss());
        assertEquals(5, result.movements().size());
        assertThrows(UnsupportedOperationException.class, () -> result.movements().clear());
        rejected(run, WRONG_PHASE, () -> run.settle(input));
        run.deposit(first, coin(3));
        assertEquals(coin(13), run.snapshot().result().orElseThrow().walletAfter());
        run.continueRound(first);
        assertTrue(run.snapshot().result().isEmpty());
        assertEquals(ROUND_ENTRY, run.snapshot().phase());
        rejected(run, STALE_INPUT, () -> run.settle(input));
    }

    @Test
    void rejectsSettlementForAnotherCommitmentAtSameRoundOrDifferentRun() {
        var run = planning(8, 0);
        var first = key(run);
        run.commit(first, allocate(3));
        var wrongAllocation = new Investment.Commitment(first, allocate(2));
        var wrongRun = new Investment.Commitment(new RoundKey(new RunId(2), 1, 1), allocate(3));
        for (var commitment : List.of(wrongAllocation, wrongRun)) {
            var bad =
                    new Investment.Settlement(
                            commitment,
                            movements(),
                            Map.of(StockId.STABLE, coin(2)),
                            special(0),
                            Optional.empty());
            rejected(run, STALE_INPUT, () -> run.settle(bad));
        }
        run.settle(settlement(run, 0, 0));
        assertEquals(-3L, run.snapshot().result().orElseThrow().profitOrLoss());
        assertEquals(coin(5), run.snapshot().wallet());
    }

    @Test
    void overflowAtCreditOrSettlementLeavesEveryOwnedValueUnchanged() {
        var walletRun = planning(Long.MAX_VALUE, 0);
        walletRun.commit(key(walletRun), allocate(1));
        unchanged(
                walletRun,
                ArithmeticException.class,
                () -> walletRun.settle(settlement(walletRun, 2, 0)));
        walletRun.settle(settlement(walletRun, 1, 0));
        walletRun.continueRound(key(walletRun));
        unchanged(
                walletRun,
                ArithmeticException.class,
                () -> walletRun.enter(entry(key(walletRun), 1, 0)));
        assertTrue(walletRun.currentEntry().isEmpty());

        var specialRun = planning(15, Long.MAX_VALUE);
        specialRun.commit(key(specialRun), allocate(2));
        unchanged(
                specialRun,
                ArithmeticException.class,
                () ->
                        specialRun.settle(
                                settlement(
                                        specialRun,
                                        3,
                                        1,
                                        Optional.of(
                                                new Shop.Rotation(
                                                        0,
                                                        List.of(
                                                                Optional.of(CHARM),
                                                                Optional.empty()))))));
    }

    @Test
    void totalLossLeavesZeroWalletAndPreservesBank() {
        var run = planning(5, 0);
        run.deposit(key(run), coin(1));
        resolve(run, 4, 0);
        assertEquals(coin(0), run.snapshot().wallet());
        assertEquals(coin(1), run.snapshot().bank());
        assertEquals(-4L, run.snapshot().result().orElseThrow().profitOrLoss());
        run.continueRound(key(run));
        run.enter(entry(key(run), 0, 1));
        assertEquals(coin(1), run.snapshot().wallet());
    }

    @Test
    void hiddenClaimsStayWithTrustedEntryAndCannotChangePublicProjectionUntilUnlocked() {
        var run = run(7, 0);
        var other = run(7, 0);
        var expected = key(run);
        var visible =
                new NewsClaim(
                        new NewsClaim.Id("visible"),
                        NewsSourceId.A,
                        StockId.STABLE,
                        NewsClaim.Polarity.POSITIVE);
        var hidden =
                new NewsClaim(
                        new NewsClaim.Id("secret"),
                        NewsSourceId.D,
                        StockId.FOLLOWER,
                        NewsClaim.Polarity.NEGATIVE);
        run.enter(entry(expected, 0, 0, visible, hidden));
        other.enter(entry(expected, 0, 0, visible));
        var first = run.snapshot();
        assertEquals(other.snapshot(), first);
        assertEquals(List.of(visible, hidden), run.currentEntry().orElseThrow().claims());
        assertFalse(first.toString().contains("secret"));
        assertThrows(UnsupportedOperationException.class, () -> first.visibleSources().clear());
        assertThrows(UnsupportedOperationException.class, () -> first.visibleNews().clear());
        run.unlockSource(expected, NewsSourceId.D);
        var unlocked = run.snapshot();
        run.unlockSource(expected, NewsSourceId.D);
        assertEquals(unlocked, run.snapshot());
        run.unlockSource(expected, NewsSourceId.E);
        assertEquals(5, run.snapshot().visibleSources().size());
        assertEquals(List.of(visible, hidden), run.snapshot().visibleNews());
        assertEquals(first.wallet(), run.snapshot().wallet());
        assertEquals(first.special(), run.snapshot().special());
        assertEquals(List.of(visible), first.visibleNews());
        unchanged(run, NullPointerException.class, () -> run.unlockSource(expected, null));
        resolve(run, 1, 1);
        run.continueRound(expected);
        assertTrue(run.currentEntry().isEmpty());
        assertTrue(run.snapshot().visibleNews().isEmpty());
        assertEquals(5, run.snapshot().visibleSources().size());
    }

    @Test
    void ownedInstancesCanShareDefinitionAndCapacityIsAtomic() {
        var run = planning(5, 3);
        var first = key(run);
        var original = run.snapshot();
        var firstCharm = owned(1);
        var secondCharm = owned(2);
        unchanged(
                run,
                IllegalArgumentException.class,
                () ->
                        run.acquire(
                                first,
                                new CharmDefinition.Owned(
                                        new CharmDefinition.InstanceId(1),
                                        new CharmDefinition.Id("unknown"))));
        run.acquire(first, firstCharm);
        rejected(run, DUPLICATE_CHARM, () -> run.acquire(first, firstCharm));
        run.acquire(first, secondCharm);
        rejected(run, INVENTORY_FULL, () -> run.acquire(first, owned(3)));
        assertEquals(List.of(firstCharm, secondCharm), run.snapshot().charms());
        assertEquals(original.special(), run.snapshot().special());
        assertTrue(original.charms().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> run.snapshot().charms().clear());
    }

    @Test
    void initialOffersCanBeSuppliedButMustMatchCapacityAndCatalog() {
        var offers = List.of(Optional.of(CHARM), Optional.<CharmDefinition.Id>empty());
        var run = GameRun.start(new RunId(1), rules(5, 0), catalog(), offers);
        assertEquals(offers, run.snapshot().shop().slots());
        assertEquals(0, run.snapshot().shop().revision());
        rejected(run, WRONG_PHASE, () -> run.consumeOffer(key(run), 0, new Shop.SlotId(1), CHARM));
        assertThrows(
                IllegalArgumentException.class,
                () -> GameRun.start(new RunId(1), rules(5, 0), catalog(), offers.subList(0, 1)));
        var unknown = List.of(Optional.of(new CharmDefinition.Id("unknown")), offers.getLast());
        assertThrows(
                IllegalArgumentException.class,
                () -> GameRun.start(new RunId(1), rules(5, 0), catalog(), unknown));
    }

    @Test
    void shopReplacementAndConsumptionRespectRevisionAndSettleOnlyOnce() {
        var run = planning(10, 0);
        var first = key(run);
        var empties = run.snapshot().shop();
        var offer = Optional.of(CHARM);
        var empty = Optional.<CharmDefinition.Id>empty();
        assertEquals(List.of(empty, empty), empties.slots());
        run.commit(first, allocate(1));
        var wrongRevision = new Shop.Rotation(1, List.of(offer, empty));
        var unknownOffer =
                new Shop.Rotation(
                        0, List.of(Optional.of(new CharmDefinition.Id("unknown")), empty));
        rejected(
                run,
                STALE_INPUT,
                () -> run.settle(settlement(run, 1, 0, Optional.of(wrongRevision))));
        unchanged(
                run,
                IllegalArgumentException.class,
                () -> run.settle(settlement(run, 1, 0, Optional.of(unknownOffer))));
        var valid = settlement(run, 1, 0, Optional.of(new Shop.Rotation(0, List.of(offer, empty))));
        run.settle(valid);
        assertEquals(1L, run.snapshot().shop().revision());
        rejected(run, WRONG_PHASE, () -> run.settle(valid));
        run.consumeOffer(first, 1, new Shop.SlotId(1), CHARM);
        assertEquals(List.of(empty, empty), run.snapshot().shop().slots());
        rejected(run, STALE_INPUT, () -> run.consumeOffer(first, 1, new Shop.SlotId(1), CHARM));
        run.deposit(first, coin(1));
        run.continueRound(first);
        run.enter(entry(key(run), 0, 0));
        assertEquals(List.of(empty, empty), run.snapshot().shop().slots());
        run.commit(key(run), allocate(1));
        run.settle(settlement(run, 1, 0, Optional.of(new Shop.Rotation(1, List.of(empty, offer)))));
        assertEquals(2L, run.snapshot().shop().revision());
        assertEquals(List.of(empty, offer), run.snapshot().shop().slots());
        assertEquals(empties.slots(), List.of(empty, empty));
    }

    @Test
    void finalDepositAndEqualityDecideStageAndEarlyGoalNeverSkipsRound() {
        var run = planning(10, 0);
        var first = key(run);
        run.deposit(first, coin(1));
        resolve(run, 1, 1);
        rejected(run, WRONG_PHASE, () -> run.closeStage(first, Optional.empty()));
        run.continueRound(first);
        playFromEntry(run);
        var second = key(run);
        run.continueRound(second);
        playFromEntry(run);
        var third = key(run);
        assertEquals(3, third.round());
        rejected(run, WRONG_PHASE, () -> run.continueRound(third));
        unchanged(
                run,
                IllegalArgumentException.class,
                () -> run.closeStage(third, Optional.of(coin(10))));
        run.closeStage(third, Optional.empty());
        assertEquals(new RoundKey(first.runId(), 2, 1), key(run));
        assertEquals(coin(2), run.snapshot().goal());
        assertEquals(coin(1), run.snapshot().bank());
        run.enter(entry(key(run), 1, 1));
        assertEquals(coin(11), run.snapshot().wallet());
        rejected(run, WRONG_PHASE, () -> run.enter(entry(key(run), 1, 1)));
    }

    @Test
    void failureIsTerminalEvenWhenLiquidWalletWouldMeetTheGoal() {
        var run = run(10, 0);
        playStage(run);
        assertEquals(coin(10), run.snapshot().wallet());
        assertEquals(coin(0), run.snapshot().bank());
        var third = key(run);
        run.closeStage(third, Optional.empty());
        assertEquals(GAME_OVER, run.snapshot().phase());
        assertFalse(run.snapshot().campaignWon());
        assertTrue(run.snapshot().phase().terminal());
        rejected(run, WRONG_PHASE, () -> run.deposit(third, coin(1)));
        rejected(run, WRONG_PHASE, () -> run.commit(third, allocate(1)));
        rejected(run, WRONG_PHASE, () -> run.enter(entry(third, 0, 0)));
        rejected(run, WRONG_PHASE, () -> run.continueRound(third));
        rejected(run, WRONG_PHASE, () -> run.closeStage(third, Optional.empty()));
        rejected(run, WRONG_PHASE, () -> run.chooseEndless(third, coin(2)));
        rejected(run, WRONG_PHASE, () -> run.finish(third));
        var investment = run.snapshot().result().orElseThrow().investment();
        var payout =
                new Investment.Settlement(
                        investment,
                        movements(),
                        Map.of(StockId.STABLE, coin(2)),
                        special(0),
                        Optional.empty());
        rejected(run, WRONG_PHASE, () -> run.settle(payout));
    }

    @Test
    void sevenStagesPreserveAssetsAndVictoryCanFinishWithoutEndless() {
        var run = campaignVictory();
        var winner = run.snapshot();
        assertEquals(VICTORY, winner.phase());
        assertEquals(7, winner.key().stage());
        assertEquals(3, winner.key().round());
        assertTrue(winner.campaignWon());
        assertFalse(winner.key().endless());
        rejected(run, WRONG_PHASE, () -> run.deposit(winner.key(), coin(1)));
        run.finish(winner.key());
        assertEquals(FINISHED, run.snapshot().phase());
        assertTrue(run.snapshot().campaignWon());
        rejected(run, WRONG_PHASE, () -> run.chooseEndless(winner.key(), coin(8)));
    }

    @Test
    void endlessRequiresExplicitIncreasingGoalsSupportsFinalDepositAndCanFail() {
        var run = campaignVictory();
        var last = key(run);
        unchanged(run, IllegalArgumentException.class, () -> run.chooseEndless(last, coin(7)));
        run.chooseEndless(last, coin(8));
        assertEquals(8, key(run).stage());
        assertEquals(ROUND_ENTRY, run.snapshot().phase());
        assertTrue(run.snapshot().campaignWon());
        playStage(run);
        assertEquals(POST_RESULT, run.snapshot().phase());
        assertEquals(coin(7), run.snapshot().bank());
        run.deposit(key(run), coin(1));
        unchanged(
                run,
                IllegalArgumentException.class,
                () -> run.closeStage(key(run), Optional.empty()));
        unchanged(
                run,
                IllegalArgumentException.class,
                () -> run.closeStage(key(run), Optional.of(coin(7))));
        run.closeStage(key(run), Optional.of(coin(9)));
        assertEquals(9, key(run).stage());
        assertEquals(1, key(run).round());
        playStage(run);
        run.closeStage(key(run), Optional.empty());
        assertEquals(GAME_OVER, run.snapshot().phase());
        assertTrue(run.snapshot().campaignWon());
    }

    @Test
    void voluntaryEndlessFinishRequiresASettledBoundary() {
        var run = campaignVictory();
        run.chooseEndless(key(run), coin(Long.MAX_VALUE));
        rejected(run, WRONG_PHASE, () -> run.finish(key(run)));
        playFromEntry(run);
        run.finish(key(run));
        assertEquals(FINISHED, run.snapshot().phase());
        assertTrue(run.snapshot().campaignWon());
    }

    @Test
    void identicalDeterministicSequencesProduceEqualSnapshots() {
        assertEquals(campaignVictory().snapshot(), campaignVictory().snapshot());
    }

    private static GameRun campaignVictory() {
        var run = run(20, 3);
        for (int stage = 1; stage <= RoundKey.CAMPAIGN_STAGES; stage++) {
            assertEquals(stage, key(run).stage());
            assertEquals(coin(stage), run.snapshot().goal());
            if (stage == 1) {
                run.enter(entry(key(run), 0, 0));
                run.acquire(key(run), owned(1));
                run.unlockSource(key(run), NewsSourceId.D);
                run.deposit(key(run), coin(7));
                resolve(run, 1, 1);
                run.continueRound(key(run));
                playFromEntry(run);
                run.continueRound(key(run));
                playFromEntry(run);
            } else {
                playStage(run);
            }
            assertEquals(3, key(run).round());
            assertEquals(POST_RESULT, run.snapshot().phase());
            run.closeStage(key(run), Optional.empty());
            assertEquals(coin(7), run.snapshot().bank());
            assertEquals(coin(13), run.snapshot().wallet());
            assertEquals(special(3), run.snapshot().special());
            assertEquals(List.of(owned(1)), run.snapshot().charms());
            assertTrue(run.snapshot().visibleSources().contains(NewsSourceId.D));
            assertEquals(stage == 7 ? VICTORY : ROUND_ENTRY, run.snapshot().phase());
        }
        return run;
    }

    private static void playStage(GameRun run) {
        for (int round = 1; round <= RoundKey.ROUNDS_PER_STAGE; round++) {
            assertEquals(round, key(run).round());
            playFromEntry(run);
            if (round < RoundKey.ROUNDS_PER_STAGE) {
                run.continueRound(key(run));
            }
        }
    }

    private static void playFromEntry(GameRun run) {
        run.enter(entry(key(run), 0, 0));
        resolve(run, 1, 1);
    }

    private static void rejected(GameRun run, GameRejection.Reason reason, Executable action) {
        var before = run.snapshot();
        var beforeEntry = run.currentEntry();
        assertEquals(reason, assertThrows(GameRejection.class, action).reason());
        assertEquals(before, run.snapshot());
        assertEquals(beforeEntry, run.currentEntry());
    }

    private static void unchanged(
            GameRun run, Class<? extends Throwable> failure, Executable action) {
        var before = run.snapshot();
        var beforeEntry = run.currentEntry();
        assertThrows(failure, action);
        assertEquals(before, run.snapshot());
        assertEquals(beforeEntry, run.currentEntry());
    }
}
