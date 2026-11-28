# Phase 03 market design addendum

The [original game design](../../GAME_DESIGN.md) calls for five stocks that can rise or fall, uncertainty influenced by news, and market effects from both visible and hidden news. It defines three rounds per stage and a seven-stage campaign with optional endless progression. Phase 02 fixed the Java identities and structural round and settlement contracts. This phase implements a fictional normalized market index and a provisional background model. See the [source traceability](traceability.md) and [Phase 02 addendum](phase-02-game-design-addendum.md).

## Decisions

| ID | Phase 03 decision |
|---|---|
| D3-01 | Keep the existing Stable, Growth, Volatile, Follower and Wildcard identities. |
| D3-02 | Use six shared regimes. Open the first round in Sideways and freeze the next mood before planning on subsequent rounds. |
| D3-03 | Carry regime, index, momentum input and history across stage boundaries. Open fresh state per run. |
| D3-04 | Choose at most one occurrence per round with an explicit no-event outcome. |
| D3-05 | Represent signed returns in integer bps and round nearest with ties away from zero. |
| D3-06 | Use a normalized index at 10¹⁰ ticks per unit. Keep effective return as the authoritative outcome. |
| D3-07 | Use versioned draw addresses and Phase 02's JDK Random seam. |
| D3-08 | Attribute mood, common noise, own noise, momentum, event and signed news separately. |
| D3-09 | Freeze background before planning. Apply only the signed impacts supplied at resolution. |
| D3-10 | Keep 64 accepted rounds by default. The planning view reveals mood and accepted history, while event and proposal details remain on trusted types. |
| D3-11 | Use fixed seeds and provisional reference thresholds for behavior testing. Full strategic balance requires the news and economy phases. |
| D3-12 | Exercise sample impacts in headless tests. The existing routes retain their unavailable state. |
| D3-13 | Use Crash and Recovery as weighted contexts. Every stock can still move either way when its other contributions outweigh drift. |
| D3-14 | Give the engine one fundamental shock per occurrence. A report can refer to that shock with zero impact. |
| D3-15 | Defer chart interpolation, persistent replay, boss rules and the endless exhaustion policy. |

## Provisional tuning

`MarketTuning.provisional()` uses `phase03-v1` and fingerprint `44030c16db9dfd0ebd240bc040753e473c6edfd7ab98434e44987285a6e44fa9`. The common noise amplitude is 700 bps. The symmetric momentum, event and news caps are 350, 1800 and 1800 bps. Final returns range from −5000 to +6000 bps.

| Stock | Archetype | Own amplitude (bps) | Common beta (‰) | Mood beta (‰) | Event sensitivity (‰) | Momentum (‰) |
|---|---|---:|---:|---:|---:|---:|
| STABLE | Defensive | 650 | 550 | 650 | 650 | 50 |
| GROWTH | Technology and momentum | 1350 | 950 | 1100 | 1150 | 130 |
| VOLATILE | Cyclical and commodity | 1850 | 850 | 950 | 1300 | 70 |
| FOLLOWER | Broad-market exposure | 950 | 1400 | 1300 | 950 | 90 |
| WILDCARD | Speculative | 2500 | 750 | 850 | 1450 | 100 |

| Prior regime | Drift (bps) | Volatility (‰) | Event chance (bps) | Bull | Bear | Sideways | High volatility | Crash | Recovery |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| Bull | 320 | 1000 | 1800 | 5400 | 800 | 2200 | 1200 | 300 | 100 |
| Bear | −320 | 1150 | 2000 | 700 | 4700 | 2300 | 1200 | 800 | 300 |
| Sideways | 0 | 900 | 1400 | 1900 | 1800 | 4900 | 1100 | 200 | 100 |
| High volatility | 0 | 1550 | 2600 | 1400 | 1500 | 2200 | 3400 | 1000 | 500 |
| Crash | −700 | 1750 | 2200 | 300 | 1400 | 1100 | 1800 | 1500 | 3900 |
| Recovery | 450 | 1250 | 1800 | 2900 | 700 | 2100 | 1000 | 300 | 3000 |

Rows sum to 10,000 probability bps. Opening a later stage follows the same transition rule as opening any subsequent round. Event chances use the selected current regime. Every event has selection weight 1. These shocks express game tuning for fictional prices.

| Event ID | Category | Targets | Shock (bps) |
|---|---|---|---:|
| CONSUMER_DISRUPTION | Sector | Stable | −520 |
| CONSUMER_RESILIENCE | Sector | Stable | 520 |
| CREDIT_RECOVERY | Sector | Follower | 750 |
| CREDIT_STRESS | Sector | Follower | −850 |
| ENERGY_DEMAND_DROP | Sector | Volatile | −850 |
| ENERGY_SUPPLY_TIGHT | Sector | Volatile | 950 |
| MARKET_PANIC | Broad market | All five | −850 |
| MARKET_RELIEF | Broad market | All five | 750 |
| PRODUCTIVITY_UPLIFT | Cross-sector | Stable, Growth, Follower | 650 |
| RATE_EASING | Macro | Growth, Follower, Wildcard | 520 |
| RATE_TIGHTENING | Macro | Growth, Follower, Wildcard | −520 |
| SPECULATIVE_SURGE | Sentiment | Wildcard | 1000 |
| SPECULATIVE_UNWIND | Sentiment | Wildcard | −1000 |
| SUPPLY_CHAIN_STRESS | Cross-sector | Stable, Growth, Volatile | −650 |
| TECH_BREAKTHROUGH | Sector | Growth | 950 |
| TECH_SETBACK | Sector | Growth | −850 |

## Open source decision

GD-12 describes false news moving contrary to its claim. Phase 03 takes already-signed influence and applies it monotonically up to saturation. Background movement and other articles can outweigh a single influence. Phase 04 must confirm whether source inversion means a signed contribution or a guaranteed final sign, then reconcile that decision with conflicts and uncertainty. The proposed signed contract maps true positive to positive, true negative to negative, false positive to negative and false negative to positive. Hidden-source influence is supplied through the same contract. Claim generation, truth, timing and causal deduplication belong to Phase 04. Settlement, flat-return mapping, and real player balance belong to later phases.

## Design traceability

| IDs | Implementation or future phase |
|---|---|
| DES3-01–06 | Five profiles with stable IDs, six moods and persistence, shared and stock-specific noise, capped momentum and a causal event catalog. Final company branding belongs to Phase 09. |
| DES3-07–08 | Aligned accepted index history and trusted attribution. Player chart rendering and explanations remain future presentation work. |
| DES3-09–10 | Signed influence seam for Phase 04. Contradiction, investigation, source histories and media literacy mechanics remain future work. |
| DES3-11–13 | Investment presentation, economy strategy and charm synergy belong to Phases 05–07 and 09. |
| DES3-14–16 | Boss stages, difficulty and seeded challenges remain backlog. Addressed draws provide the deterministic prerequisite for replay. |
| DES3-17–18 | Art direction, motion and accessibility belong to Phase 09. Vertical slice playtests require the real loop in Phases 05–08. |

[Architecture and replay](../architecture/market-simulation.md) · [Verification and statistical observations](../engineering/phase-03-evidence.md)
