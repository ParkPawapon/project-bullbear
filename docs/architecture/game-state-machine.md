# Phase 02 game state machine

The run key is a caller-supplied positive run ID plus one-based stage and round indices. Every transition checks the key and phase. `closeStage` atomically assesses the bank when the final deposit window closes.

```mermaid
stateDiagram-v2
    [*] --> ROUND_ENTRY: start at stage 1 round 1
    ROUND_ENTRY --> PLANNING: enter with valid supplied input
    PLANNING --> PLANNING: deposit or trusted stable interaction
    PLANNING --> INVESTMENT_COMMITTED: commit positive affordable allocation
    INVESTMENT_COMMITTED --> POST_RESULT: settle the exact commitment once
    POST_RESULT --> POST_RESULT: deposit or trusted stable interaction
    POST_RESULT --> ROUND_ENTRY: continueRound when round is 1 or 2
    POST_RESULT --> GAME_OVER: closeStage after round 3 when bank is below the goal
    POST_RESULT --> ROUND_ENTRY: closeStage after round 3 and advance to a higher goal
    POST_RESULT --> VICTORY: closeStage after round 3 of stage 7 when goal is met
    VICTORY --> ROUND_ENTRY: chooseEndless with higher goal at stage 8
    VICTORY --> FINISHED: finish
    POST_RESULT --> FINISHED: finish at a settled endless boundary
    GAME_OVER --> [*]
    FINISHED --> [*]
```

| Command | Guards and state effects |
|---|---|
| `enter` | Exclusive to entry. Apply the supplied credit to the wallet before planning. Initial funds cover the first round. |
| `deposit` | Planning and post-result. Transfer the positive affordable amount to the bank atomically. Drafts are detached values and affordability is recalculated on confirmation. |
| `commit` | Planning. Debit the stake once and retain the exact immutable commitment. |
| `settle` | Committed. Validate and apply one payout, optional reward and optional complete offer rotation. Clear the stake and keep a safe result. |
| `continueRound` | After settlement of round 1 or 2. Retire round-local data and open a new entry. |
| `closeStage` | After settlement of round 3. Bank equality passes. Campaign goals use the configured schedule. An endless success requires a supplied higher goal. |
| `chooseEndless`, `finish` | Victory offers an explicit choice. Endless can finish at a settled boundary. A terminal run remains readable. |

Expected command rejections use `GameRejection.Reason` for phase, stale identity, insufficient Coin and inventory failures. Malformed values and incompatible supplied data use Java argument errors. Checked arithmetic reports `ArithmeticException`. The run computes replacements before publishing state. A later phase can translate failures to localized feedback.

The campaign result remains true in endless mode, including after an endless loss. The terminal state permits a new independent run in `GameSession` with a strictly higher supplied ID.
