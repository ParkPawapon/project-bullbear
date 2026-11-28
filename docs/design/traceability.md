# Design traceability

[GAME_DESIGN.md](../../GAME_DESIGN.md) is the original source. The three identifier sets use the design counts and taxonomy plus the working identifier names in the foundation brief. Five named stocks and five source letters represent catalog metadata. D and E are reserved source identifiers. The four shell routes expose unavailable feature placeholders.

| ID | Design requirement | Evidence or future owner |
|---|---|---|
| GD-01 | Read news and buy stocks without short-selling | Documented. Investment use case and domain validation later. |
| GD-02 | Meet the monetary goal within three investment selections per stage | Documented. Progression later. |
| GD-03 | Increase the goal each stage | Documented. Progression later. |
| GD-04 | Award special currency when the player profits | Documented. Settlement and charm economy later. |
| GD-05 | Win at stage seven with optional endless continuation | Documented. Progression later. |
| GD-06 | Provide five stocks | `StockId` and catalog. Working identifiers are Stable, Growth, Volatile, Follower, and Wildcard. |
| GD-07 | Random stock movement with directional probability influenced by news | Phase 03 simulates all five stocks and accepts resolved signed influence. Phase 04 supplies it from news claims. |
| GD-08 | Choose news origin randomly among five sources | `NewsSourceId` and catalog hold only A–E. Selection later. |
| GD-09 | Initially reveal three sources, with charms unlocking others | Documented. Application visibility policy later. |
| GD-10 | Hidden news still influences the market | Phase 03 applies signed impacts independently of the player projection. Phase 04 supplies the full impact set. |
| GD-11 | Sources have base reliability and profit rate | Documented. A/B/C examples are qualitative. Values and rules are open. |
| GD-12 | False news causes movement opposite to the stated direction | Phase 04 owns truth and the source interpretation recorded in the [Phase 03 addendum](phase-03-market-design-addendum.md). |
| GD-13 | Purchase passive charms with the special currency | Documented. Shop and economy later. |
| GD-14 | Keep the shop available throughout play per design | Documented. Shop use case later. |
| GD-15 | Leave a purchased slot vacant until an investment completes | Documented. Shop refresh rules later. |
| GD-16 | Reroll all shop slots after each investment | Documented. Shop refresh rules later. |
| GD-17 | Limit charm inventory | Documented. Capacity is open. |
| GD-18 | Information, source-specific, and conditional multiplier charms | `CharmCategory` and catalog. Category effects later. |
| GD-19 | Deposit toward the goal during or between investments and before stage end | Documented. Bank use case later. |
| GD-20 | Prohibit withdrawals | Documented. Bank invariant later. |
| GD-21 | Pay interest at each new round at a rate increasing with deposits | Documented. Interest policy later. |
| GD-22 | Credit interest to the wallet | Documented. Economy later. |
| GD-23 | Cap interest and grant a small minimum income per round | Documented. Amounts and formula are open. |
| GD-24 | Start with an open Coin amount and keep allocations within the wallet | Documented. Economy and validation later. |
| GD-25 | Show all five stock movements when confirming an investment | Phase 03 produces five aligned movements. Confirmation, settlement and presentation integration follow in Phase 05. |
| GD-26 | Show money returned or lost and special currency awarded, then replenish the shop | Documented. Settlement, results and shop later. |
| GD-27 | Choose whether to deposit or retain cash before the next interest payment | Documented. Bank and round orchestration later. |
| GD-28 | Compare bank to goal at three rounds. Advance with charms on success or restart after failure. | Documented. Progression and economy later. |
| GD-29 | Boss-stage rule variants | Optional idea, deferred. |
| GD-30 | One-use item that reveals a news item's truth | Optional idea, deferred. |
| GD-31 | Inspirations include CloverPit and Balatro | Reference only. Bullbear has its own design and assets. |

## Visual and Thai baseline

The brief describes a dark navy retro desktop, four entry points, a `news.net` concept and a five-row `stocks.exe` concept. The shell adopts only geometric controls and a restrained palette. The example balance, allocations, returns, timestamps, headlines, charts, and sample source colors are visual examples. Reference images can remain in design documentation once identified and approved.

| Reference copy | Future rendering evidence |
|---|---|
| ข่าว | Tone-mark positioning |
| หุ้น | Upper-mark placement |
| ร้านค้า | Combined marks and glyph layout |
| ธนาคาร | Thai alphabet coverage |
| ยืนยันการซื้อ | Combining marks |
| รอบ 1/3 · ด่าน 1 | Thai, ASCII digits, and punctuation |
| Stable ประกาศกำไรไตรมาสนี้โตเล็กน้อย | Mixed Thai/Latin shaping and wrapping |

Verify a licensed Thai font, marks and line breaks on the target platforms. The foundation's English bitmap font suffices for the present shell. A future shaping-aware renderer can be evaluated when Thai UI is in scope.

## Proposed differentiation

The design combines incomplete information, source credibility versus payoff, and the chance that a false story reverses movement. Irreversible deposits trade current investment capital for goal progress and future wallet income. Three charm categories alter information access, specific sources, or profit under conditions. These choices recur over a constrained number of rounds, rising stage goals, and an optional endless continuation. This is a proposed combination for a fictional exercise in uncertainty and risk management. Comparative research and playtesting remain open.

## Deferred decisions

- Official title, branding, localization, accessibility, music, and final asset licensing.
- Initial Coin, special currency name and rewards, goal schedule, wallet/bank carryover, and settlement order.
- Return ranges, stock behavior and correlations, news count and distribution, conflicts, reliability/profit parameters, and modifier caps.
- Shop size, inventory capacity, prices, catalog, duplication, replacement, stacking and animation-time availability.
- Zero-allocation handling, allocation granularity, rounding, maximum balances, and exact deposit timing.
- Interest formula, cap, minimum income, and first-round and stage-transition timing.
- Save format, replay determinism, difficulty, and whether to include boss rules and consumables.

## Phase 02 structural mapping

Source IDs retain their original meanings. The phase evidence maps the implementation to its test scenarios, and the design addendum records future proposals and their phases. [Evidence and test mapping](../engineering/phase-02-evidence.md) · [Design addendum](phase-02-game-design-addendum.md)

| Source ID | Phase 02 contract and proof | Remaining phase |
|---|---|---|
| GD-01, GD-24 | Whole Coin budget and buy-only allocation with commitment revalidation. `ValueContractsTest` and `GameRunTest`. | 05 user flow and trading resolution |
| GD-02, GD-03, GD-05, GD-28 | Strictly increasing seven-stage goals, three settlements per stage, exact final bank comparison, victory, game over and optional endless. `GameRunTest` and `GameSessionTest`. | 08 playable coordination and approved tuning |
| GD-04, GD-26 | Trusted payout and profit-only special reward, safe result and optional rotation. `ValueContractsTest` and `GameRunTest`. | 05 reward formula and presentation, 07 actual offer generation |
| GD-06, GD-07, GD-25 | Five canonical `StockId`s and complete movement data. `ValueContractsTest`. | 03 market behavior, 05/09 display |
| GD-08, GD-09, GD-10, GD-11 | Five source definitions, three initial sources, monotonic unlock and a projection that preserves trusted hidden data. `ValueContractsTest` and `GameRunTest`. | 03/04 sampling and news influence, 07 charm use |
| GD-12 | Future inversion and conflict handling recorded in the addendum. | 03/04 truth and market influence |
| GD-13, GD-17, GD-18 | Typed paid charm definitions, unique owned identities, capacity and the existing distinct categories. `ValueContractsTest` and `GameRunTest`. | 07 ownership transaction and effects |
| GD-14, GD-15, GD-16 | Stable-phase availability, revision-checked vacancy and optional complete replacement at settlement. `GameRunTest`. | 07 generated rotation and purchase, 09 UI |
| GD-19, GD-20, GD-27 | Irreversible wallet-to-bank transfer during planning and post-result, including the final window. `GameRunTest`. | 06 bank use case and feedback |
| GD-21, GD-22, GD-23 | Supplied round credit goes to the wallet once. First-round policy recorded separately. `GameRunTest`. | 06 interest and minimum-income calculations |
| GD-29, GD-30 | Optional boss variants and one-use truth reveal described as proposals. | 07/08 owner decision and experiments |
| GD-31 | Original references remain contextual. | Continue original design and assets |
