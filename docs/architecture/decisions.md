# Foundation decisions

| Decision | Reason and consequence |
|---|---|
| Adopt the existing `project-bullbear` repository identity | Use `game.bullbear` as the development namespace and `bullbear` as the ASCII launcher name. The game title and branding remain product decisions. |
| Java 21, Gradle, libGDX and LWJGL3 | The inner layer supports ordinary Java tests and the outer layer provides a native desktop window with a 2D rendering and input stack. |
| One screen with a small route model | Four unavailable destinations share one layout and one owned stage. Dedicated screens can follow when they need distinct behavior. |
| Fixed logical viewport and runtime colors | `FitViewport` keeps the 1280 × 720 logical aspect ratio as the window changes. A generated solid texture and libGDX bitmap font are enough for the foundation. |
| English shell, runtime graphics smoke by opt-in | The default font covers the English UI and the smoke path renders with real native graphics. Thai rendering and default headless checks have separate acceptance criteria. |
| Install locally using the Gradle application plugin | The generated launcher works from another directory, and still requires Java. Native app images, installer signing, and publication belong to a release phase. |

## Phase 02 core domain decisions

The proposal IDs come from the scoped Phase 02 brief. Source constraints come from the [original design](../../GAME_DESIGN.md). Structural choices are model contracts that can be revised with their tests before a future save format exists.

| ID and origin | Adopted choice and reason | Alternative or future decision |
|---|---|---|
| D-01 · PROPOSED | Bank, liquid Coin, special currency, inventory, source access and shop state carry. The bank is cumulative because players cannot withdraw. | A stage-reset economy needs an explicit design change. Goals remain supplied. |
| D-02 · SOURCE + PROPOSED | Buy-only allocations lock a one-round stake. A fixed stock enum is the present stock definition. | Persistent share lots, margin and order books are outside the loop. |
| D-03 · PROPOSED | Separate whole-unit `long` currencies prevent mixing and support exact arithmetic. A signed result is derived from returned Coin and the stake. | Fractions would need explicit fixed precision and rounding rules. |
| D-04 · PROPOSED | The first round uses starting funds. Later entries apply distinct supplied interest and income to the wallet once. | Phase 06 may revisit first-round credit and must define amount, rounding and cap. |
| D-05 · SOURCE + PROPOSED | Rules supply three distinct visible sources. Trusted unlock is monotonic and a duplicate leaves the same state. | A/B/C is a fixture choice. Temporary boss overrides and actual unlock timing come later. |
| D-06 · PROPOSED | Zero entries are omitted. A detached draft can be empty, and confirmation requires a positive affordable total. | A deliberate pass would need separate anti-farming and progression rules. |
| D-07 · PROPOSED | Trusted callers supply credits, claims, five movements, per-stock payouts and any reward. News credibility is 0–10,000 basis points and impact is nonnegative whole basis points through the representable `long` range. | Phase 03–07 engines choose parameters and tighter operational limits. |
| D-08 · PROPOSED | Deposit and trusted ownership/shop interactions use planning and post-result windows. Resolution freezes those mutations. | Animation and purchase intent policy belong to later UX. |
| D-09 · PROPOSED | Commit snapshots allocation and key and debits exactly once. Later settlement must agree with that commitment. | Future charm effects need a corresponding frozen snapshot. |
| D-10 · SOURCE | Settlement includes all five directions with `UP` or `DOWN`. Payouts include exactly the positively invested stocks. | A neutral direction requires an explicit market decision. |
| D-11 · SOURCE + PROPOSED | Explicit final review happens atomically after the third settlement. The bank alone determines success and equality passes. | A separate review screen could use the post-result snapshot. |
| D-12 · SOURCE | Stage 7 success opens an explicit victory choice and does not automatically advance. | Later UX decides how to present victory and continuation. |
| D-13 · PROPOSED | Endless advances using a supplied strictly higher goal. A settled endless round is a safe voluntary finish. | A difficulty curve and extended history are future choices. |
| D-14 · SOURCE + PROPOSED | Typed charm IDs prevent ownership duplication. Separate instances may share a definition. Full inventory rejects. Slot and inventory capacities use a 1–1,024 technical range. | Prices, capacity tuning, stacking and replacement policy remain configurable or deferred. |
| D-15 · PROPOSED | Commands use explicit keys and carry no time source. | Future display timing can remain independent of the run. |
| D-16 · EXISTING + PROPOSED | Keep fictional, offline play and local service boundaries. | New data collection or online services would require separate design authorization. |
| D-17 · PROPOSED | One application-owned run. A terminal run can be replaced using a higher caller-supplied ID. | An abandon action, profiles, persisted IDs and replay are future contracts. |
| D-18 · PROPOSED | Exact arithmetic rejects overflow before mutation. The positive `int` stage number and positive `long` next goal have finite limits. | At the maximum goal, a further increase is rejected and the prior safe endless result remains available to finish. |

The shop starts empty or with a supplied complete layout. An offered slot can be consumed by trusted code with a matching revision. A supplied complete rotation is applied only during successful settlement, with an incremented revision. Omitting a rotation preserves the layout. Phase 07 must supply actual offers and combine payment, ownership and slot consumption atomically. Duplicates within a Java map are structurally impossible. A future parser must reject duplicates before conversion.

News visibility filters only the player snapshot. Trusted round entries retain every claim while the round is current. Truth and the exact reveal policy require Phase 04. Existing seeds remain in the application random factory. The core makes no random draws and retains the current round rather than a growing run history.
