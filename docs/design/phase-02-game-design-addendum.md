# Phase 02 design addendum

The [original design](../../GAME_DESIGN.md) establishes five stocks, five sources, a three-source opening, three rounds per stage, a seven-stage campaign, and optional endless play. The [decision record](../architecture/decisions.md#phase-02-core-domain-decisions) identifies the adopted structural choices. All ideas below describe future work.

| Idea | Priority and status | Proposal and proof needed |
|---|---|---|
| DES-01 Stock personalities | P1 · PROPOSED · Phase 03 | Stable offers a narrower return range, Growth responds to sustained positive evidence, Volatile has wider outcomes, Follower reacts to shared market conditions, and Wildcard has distinct bounded tail risk. Use fixed-seed statistical tests and compare strategies for dominance. |
| DES-02 Market mood | P1 · PROPOSED · Phase 03/08 | Consider readable bullish, bearish, uncertain and stress contexts. Keep mood consistent after commit and independent of individual hidden headlines. Verify bounded outcomes and recovery options. |
| DES-03–05 Credibility and conflicting news | P0/P1 · PROPOSED · Phase 04 | Give A–E reliability versus impact niches. Author fictional contradictory signals and define deterministic combination and caps. True and false claims retain the source design's directional meanings. A result recap may reveal what policy permits and distinguish news from background movement. The engine consumes all claims. |
| DES-06 Commitment and feedback | P1 · PROPOSED · Phase 05/09 | Review allocation and liquidity before confirmation. Reveal all five moves with an optional short animation, then show stake, returned Coin, net result and reward. Offer keyboard and reduced-motion controls. Simulation remains independent of animation. |
| DES-07–09 Banking and recovery | P0/P1 · PROPOSED · Phase 06 | Show the trade-off between protected progress and liquid capital. Explore bounded, diminishing interest that credits the wallet and a predictable minimum income. Test low-capital recovery and representative early versus late deposit strategies. |
| DES-10–13 Charm builds | P0/P1 · PROPOSED · Phase 07 | Information charms could unlock a source or increase later articles. Source charms could specialize in credibility or impact. Conditional charms could reward concentration or a meaningful diversified allocation. Define timing, stacking order, caps and invalid-purchase behavior. Preserve slot vacancy until successful settlement. |
| DES-14 Campaign pacing | P1 · PROPOSED · Phase 08 | Introduce basic allocation and bank decisions in stage 1, credibility in stage 2, early charm choices in stage 3, conflicting evidence in stage 4, liquidity pressure in stage 5, a telegraphed stress test in stage 6, and a culminating goal in stage 7. Validate the pacing through playtests. |
| DES-15 Boss variants | P2 · OPTIONAL_EXPERIMENT · Phase 08 | Prototype one clearly signaled temporary risk or usable-source modifier. Preserve permanent unlocks and hidden market influence. Check attainability before expanding. |
| DES-16 Endless | P1 · PROPOSED · Phase 08 | Offer a deliberate continuation and voluntary finish, preserve the campaign result, and avoid unbounded history or numeric inflation. Tune increasing goals only after validating campaign play. |
| DES-17–18 Learning and recap | P1 · PROPOSED · Phase 08/09 | Give a short optional guide and a local end-of-run summary based on actual results. Verify dynamic Thai layout, accessible controls, and the privacy of any recorded data. |
| DES-19 Repeatability | P2 · OPTIONAL_EXPERIMENT · Phase 08/10 | Consider seeded local practice and versioned replay after deterministic engines exist. Validate rules, algorithm and stream versions across restored sessions. |
| DES-20 Consumable | P2 · OPTIONAL_EXPERIMENT · Phase 07/08 | Consider one explicitly scoped truth reveal with one-use ownership and no reroll. Decide timing, information policy and interaction with passive charms first. |
| DES-21 Fair play | P0 · PROPOSED | Use fictional currency, offline play, clear exits and readable uncertainty. Keep real-money wagers, paid randomness, hidden post-confirmation odds changes and external telemetry outside the design. |
| DES-22 Balancing | DEFERRED to working engines and playtests | Compare fixed-seed bank-first, concentration, diversification, source-informed and charm-focused strategies. Measure survival, decision time, win distribution and recovery. Set goals from evidence. |

### Implementation handoff

| Phase | Structural input | Next work |
|---|---|---|
| 03 Market | Existing `StockId`, `Investment.Commitment`, movement contract and `SimulationRandom.seeded` | Implement a deterministic market engine with documented strategy and statistical checks. Separate future gameplay streams from cosmetic randomness. |
| 04 News | `GameCatalog.SourceDefinition`, `RoundEntry`, `NewsClaim` and filtered `GameSnapshot` | Generate authoritative claims and truth. Freeze the round's data and pass every claim to market resolution. |
| 05 Investment | `Investment.Allocation`, `Investment.Settlement` and `GameRun` commit/result boundary | Compute per-stock returns and rewards. Validate real settlement and result presentation. |
| 06 Economy | `CoinAmount`, `SpecialCurrencyAmount`, protected `GameRun.deposit` and `RoundEntry` | Define interest, cap, income and deposit UX against approved tuning. |
| 07 Charms and shop | `CharmDefinition`, owned instances, `Shop` and supplied rotation | Define atomic purchase, category effects, duplicate and stacking policy, and generated offers. |
| 08 Progression | `GameRun`, `GamePhase`, `RoundKey` and `GameSession` | Integrate actual round orchestration, replay and optional variants with bounded state. |
| 09 UX | `GameSnapshot` and the existing shell | Implement playable screens, accessibility, Thai/English content and licensed audio. |
| 10 Release | Existing build and packaging workflow | Validate platform coverage and owner-approved publication prerequisites. |
