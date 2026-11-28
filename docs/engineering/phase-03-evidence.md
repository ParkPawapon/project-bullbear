# Phase 03 verification evidence

Verification date 2026-11-28 (Asia/Bangkok). **Status** `COMPLETE_LOCAL_WITH_EXTERNAL_PENDING`.

## Baseline and gates

The baseline was `e5129a85aa6c0be4ebd93efacc2e90a838ac4971` on `main`, with an empty staged diff. Windows `gradlew.bat --version` and `gradlew.bat --console=plain --no-build-cache clean check` established Gradle 9.9.3, Temurin 21.0.12+8 and 78 passing baseline tests. The desktop uses libGDX 1.14.2 and LWJGL 3.3.3. Phase 02 gameplay contracts and its evidence remain historical.

| Gate | Result | Verified work |
|---|---|---|
| G0 | PASS | Baseline, five existing stock identities, round and settlement contracts, RNG seam and original design fingerprint. |
| G1 | PASS | Domain math, canonical validated rules, bounded state, event and impact ownership, exact recurrence and rejection paths. |
| G2 | PASS | Headless application replay and acceptance with the existing seed factory. Existing locked-news projection and run regressions passed. |
| G3 | PASS | Four isolated experiments, 1,000 natural campaign-length sequences and the measured benchmark below. |
| G4 | PASS | Fresh Windows and WSL builds executed Spotless, SpotBugs, JaCoCo and `verifyArchitecture`. |
| G5 | PASS locally | Strict artifact verification and dependency locks passed. Live OSV and a redacted Gitleaks scan ran against their scoped inputs. |
| G6 | PASS locally | Installed Windows and WSLg launchers rendered the shell and exited 0. Windows ZIP and TAR were built. |
| G7 | PASS | Design hash and staged diff verified. No production changes in desktop, presentation, build configuration, locks or verification metadata. |
| G8 | PASS locally | [Architecture](../architecture/market-simulation.md), [design decisions](../design/phase-03-market-design-addendum.md), traceability and this report. |

### Build and runtime

- Native Windows 11 Pro x64 build 26300 ran `gradlew.bat --console=plain --no-build-cache clean check build :desktop:installDist`. Exit 0 in 23 seconds with all 52 tasks executed.
- Ubuntu 26.04 x86-64 under WSL2 with kernel `6.18.33.2-microsoft-standard-WSL2` ran `env -u DISPLAY -u WAYLAND_DISPLAY ./gradlew --no-daemon --console=plain --no-build-cache clean check build :desktop:installDist` in an independent source copy. Exit 0 in 20 seconds with 51 tasks executed and one up to date. Both builds used Temurin 21.0.12+8.
- Both installed launchers ran `--smoke-test` from separate working directories with isolated preferences and printed `FOUNDATION_SMOKE_OK`. WSL rendered through WSLg. The smoke exercises the existing route listeners and returns to Home. Market and replay tests run without graphics.
- `jdeps -s` found `java.base` for the installed domain JAR and `java.base` plus domain for application. Their JAR listings contained 25 and 2 production market class files respectively. Test fixtures, the experiment runner, prompt and design files are absent from both runtime JARs.
- The existing Gradle build emits a deprecation warning from the build-tool stack. The phase leaves that stack and its quality policy intact.

| Local Windows distribution in `desktop/build/distributions/` | Bytes | SHA-256 |
|---|---:|---|
| `bullbear-0.1.0.zip` | 13,833,687 | `E8054DCEA41E648C4163301878C72A8F9373EE06934D8D20871FE7BBDF4FD403` |
| `bullbear-0.1.0.tar` | 14,396,416 | `132D21773EFFC44211498F05CA27BE0D5C3C629FB6B9D4A90CDC34ADEAEF3D50` |

### Test and coverage results

Each system passed **105 tests**, with zero failures, errors or skips. Phase 03 adds 27 deterministic tests. Coverage is from the final Windows XML reports.

| Scope | Tests | Lines | Branches |
|---|---:|---:|---:|
| domain | 52 | 780/783 (99.62%) | 226/232 (97.41%) |
| application | 13 | 64/66 (96.97%) | 14/14 (100%) |
| presentation | 18 | 41/357 (11.48%) | 8/92 (8.70%) |
| desktop | 22 | 173/261 (66.28%) | 54/95 (56.84%) |
| `domain.market` | 22 | 397/400 (99.25%) | 78/80 (97.50%) |
| `application.market` | 5 | 39/41 (95.12%) | 6/6 (100%) |

Module-relative evidence is in `build/test-results/test/TEST-*.xml`, `build/reports/jacoco/test/jacocoTestReport.xml` and `build/reports/spotbugs/`. All eight SpotBugs XML reports contained zero high-confidence findings.

### Supply chain and integrity

- The unchanged lockfiles and SHA-256 verification metadata resolved in strict builds. `python .github/scripts/audit_dependencies.py` queried live OSV on the verification date and returned all 225 Maven coordinates with zero known advisory matches. Report `build/reports/security/osv.txt`. This scope covers the Maven inventory. JDK, Actions and native binaries embedded in libraries require separate vendor coverage.
- Gitleaks 8.30.1 scanned the 22 Phase 03 source, test and documentation files with redaction and found zero leaks. Local editor settings and the pre-existing Wrapper timestamp are outside this scope.
- `GAME_DESIGN.md` SHA-256 before and after was `B604FB3EA4AE95AC3942F52FDD089CF4D06DFC39D1A9820288FF96B20BFC06D0`. `git diff --check` passed. The index remained empty, local HEAD remained at baseline, and no commits, pushes, merges or repository settings changes were made. The pre-existing `.vscode/` and Wrapper timestamp edit remained in the local tree. The Unix launcher tracked mode is `100755`.

## Requirement and scenario traceability

| IDs | Result and evidence |
|---|---|
| MKT-01–02, CFG-01–03, T001–007, T018, T051–052, T055 | PASS. `MarketRulesTest.defaultCatalogCoversEveryStockWithFavorableAndAdverseEvents`, `invalidProfilesAndSettingsAreRejectedAtConstruction`, and `MarketEngineTest.openingProjectionContainsAllStocksAndNoResultOrLatentEvent`. Exact typed stock set, duplicate keys rejected by the input map contract, five outputs and an allocation-free engine signature. The formula fixture isolates own noise. |
| MKT-03–05, CFG-04–05, T008–014, T023–027, T037–050 | PASS. `MarketRulesTest.transitionTableRejectsMissingDuplicateOutOfRangeAndIncorrectTotals`, `invalidEventCatalogAndIdentityAreRejected`, and `MarketEngineTest` methods for transition boundaries, every default row, mood/volatility, frozen event choice, favorable/zero chance and capped maximum noise. |
| MKT-06, CFG-06–07, T015–017, T028–036, T098, T105 | PASS. `MarketRulesTest` canonical identity and content change tests. `MarketSessionTest` golden vector, addressed coordinates, repeat replay across stages and IDs, and seed diversity tests. Golden passed on both OS environments. |
| MKT-07, T019–022, T053–068, T095 | PASS. `MarketMathTest` signed rounding and tick limits. `MarketEngineTest` worked oracle, capped momentum/returns, both 21-round extremes and numerical limit retry. |
| MKT-08, HST-01–07, T069–070, T083–084, T087–098 | PASS. `MarketEngineTest` opening projection, defensive maps, stale predecessor, numerical failure and history eviction. `MarketSessionTest.onlyOneProposalCanAdvanceTheOwnerAndNoOtherRunCanPromoteIt` exercises promotion. Wrong/missing stock state coverage is rejected at snapshot construction. |
| MKT-09, IMP-01–09, T071–082, T086 | PASS. `MarketEngineTest.signedImpactAggregationIsOrderIndependentIsolatedAndImmutable`, `malformedImpactInputsFailWithoutAcceptingAnything`, `signedImpactsCanReverseTheBackgroundAndSaturateMonotonically`, and frozen event tests. Synthetic inputs exercise the signed contract. Actual hidden claims and upstream causal deduplication are Phase 04 work. |
| MKT-10, INT-01–08, T085, T099–101 | PASS locally. Existing `GameSessionTest`, `GameRunTest`, `ValueContractsTest` and architecture check remain green. Illegal settlement is covered by the existing aggregate tests. Cross-aggregate promotion is a Phase 05 integration contract. |
| MKT-11, PERF-01–06, S01–18 | PASS for the local scope. Reproducible experiments and individual timing samples below, plus bounded-history and numerical-limit tests. |
| MKT-12, T102–104, T106 | PASS locally. Existing shell tests, installed native smoke on Windows and WSLg, package inspection and original design/index checks. |

## Statistical observations

`MarketExperiment` lives in test sources and can run with compiled test, application and domain class directories. Run from the repository root.

```powershell
.\gradlew.bat :application:testClasses
$marketClasses = 'application/build/classes/java/test;application/build/classes/java/main;domain/build/classes/java/main'
java -Xmx384m -cp $marketClasses game.bullbear.application.market.MarketExperiment
java -Xmx384m -cp $marketClasses game.bullbear.application.market.MarketExperiment --benchmark
```

Use `./gradlew` and separate the same class directories with `:` on POSIX. The benchmark switch is optional. No-argument mode uses fixed seeds and reports independently from elapsed time.

- Isolated experiments use 20,000 independent opening rounds per mood for seeds 0–19,999. The validated fixture changes the opening mood and disables event probability and momentum. There are no external impacts. Different fixture fingerprints produce different addressed draws, so the comparisons below are unpaired. The exact same-draw contrasts are tested in `MarketEngineTest`.
- Natural sequences use default rules `phase03-v1` and 1,000 seeds, 0–999, with 21 accepted rounds each. The engine samples the actual transitions and events. No external impacts are supplied. Events occurred in 3,873 of 21,000 rounds. A round counts as eventful whether or not the event targets each particular stock.
- The results use population standard deviation and nearest-rank percentiles. Common correlation is Pearson between effective return and that stock's scaled common contribution. Pairwise correlation is Pearson between stock returns. The report uses exact integer counts and formatted decimals. Streaks are consecutive observed same-mood rounds, clipped at campaign boundaries. Transition comparisons are conditional on the source mood and summarize correlated observations in a chain.
- Windows and WSL reports matched exactly after normalizing newlines to LF and trimming trailing line endings, with SHA-256 `103D6BF70EEFA272BA347FF0329433EFBF8C647DE72086B53BE45DC93228CBE8`.

| Hypotheses | Result |
|---|---|
| S01–02 | No bounds or attribution violations across 101,000 simulated rounds. Exact 21-round loss and gain sequences finish at 476,838 and 19,342,813,113,834,086 ticks respectively. |
| S03–06 | Both neutral signs exceed 10% for every stock. Wildcard standard deviation is 3.3628× Stable. Follower common beta is 1400‰ against Stable's 550‰ and has the highest neutral common correlation at 0.7108. Risk and exposure differ across the five profiles. |
| S07–08 | High-volatility median standard deviation ratio is 1.7266× Sideways. Each Bull mean exceeds its corresponding Bear mean. Controlled mood contrasts also pass. |
| S09–11 | Weighted boundary and exact 0/10000 probability tests pass. Observed event rates and transition rows fall within the pre-established `max(0.01, 5√(p(1−p)/n))` tolerance. |
| S12 | Neutral pairwise correlations are all positive, 0.0583–0.3658. |
| S13–14 | Paired signed fixtures move returns in the expected direction before and through saturation. Opposite 100 bps backgrounds become signed 25 bps outcomes when given opposing 125 bps impacts. |
| S15–16 | Zero isolated cap hits and one lower cap hit for Wildcard in 21,000 natural rounds. The reference metrics show different mean, risk and tail tradeoffs. Strategic dominance and difficulty remain open until real payouts, choices and news are integrated. |
| S17–18 | 1,000 of 1,000 natural return sequence signatures are distinct. Both systems reproduced the golden vector and the full fixed-seed report. |

<details>
<summary>Full deterministic statistics report</summary>

```text
isolated SIDEWAYS fingerprint=35eb9dae545f8674c94e1e4724834b26c43f77c35bcbf7471b329163c891503a seeds=0..19999
STABLE n=20000 min=-834 max=899 mean=-0.43 sd=279.48 signs(+/-/0)=10011/9971/18 p05/50/95=-464/1/459 caps(low/high)=0/0 common=0.5097
  corr(STABLE,GROWTH)=0.2229
  corr(STABLE,VOLATILE)=0.1627
  corr(STABLE,FOLLOWER)=0.3658
  corr(STABLE,WILDCARD)=0.1024
GROWTH n=20000 min=-1710 max=1657 mean=3.08 sd=549.54 signs(+/-/0)=10034/9942/24 p05/50/95=-914/3/906 caps(low/high)=0/0 common=0.4397
  corr(GROWTH,VOLATILE)=0.1358
  corr(GROWTH,FOLLOWER)=0.3102
  corr(GROWTH,WILDCARD)=0.0834
VOLATILE n=20000 min=-2105 max=2019 mean=-0.74 sd=713.06 signs(+/-/0)=10012/9978/10 p05/50/95=-1192/2/1186 caps(low/high)=0/0 common=0.3098
  corr(VOLATILE,FOLLOWER)=0.2162
  corr(VOLATILE,WILDCARD)=0.0583
FOLLOWER n=20000 min=-1595 max=1653 mean=-1.63 sd=500.98 signs(+/-/0)=9965/10022/13 p05/50/95=-837/-3/827 caps(low/high)=0/0 common=0.7108
  corr(FOLLOWER,WILDCARD)=0.1366
WILDCARD n=20000 min=-2532 max=2557 mean=-14.43 sd=939.85 signs(+/-/0)=9880/10109/11 p05/50/95=-1561/-14/1569 caps(low/high)=0/0 common=0.2071
index ticks min=746800000000 max=1255700000000 violations=0
isolated HIGH_VOLATILITY fingerprint=6cebc5db1943298fbb7bd5b06393ca4240ef47eb87b7df2ec5dd39c7549d4862 seeds=0..19999
STABLE n=20000 min=-1441 max=1454 mean=2.78 sd=476.85 signs(+/-/0)=10024/9952/24 p05/50/95=-794/2/791 caps(low/high)=0/0 common=0.5056
  corr(STABLE,GROWTH)=0.2140
  corr(STABLE,VOLATILE)=0.1546
  corr(STABLE,FOLLOWER)=0.3675
  corr(STABLE,WILDCARD)=0.1012
GROWTH n=20000 min=-2850 max=2839 mean=5.10 sd=953.80 signs(+/-/0)=10068/9922/10 p05/50/95=-1577/10/1588 caps(low/high)=0/0 common=0.4294
  corr(GROWTH,VOLATILE)=0.1264
  corr(GROWTH,FOLLOWER)=0.3002
  corr(GROWTH,WILDCARD)=0.0795
VOLATILE n=20000 min=-3473 max=3460 mean=-0.79 sd=1231.17 signs(+/-/0)=9971/10021/8 p05/50/95=-2040/-4/2058 caps(low/high)=0/0 common=0.3072
  corr(VOLATILE,FOLLOWER)=0.2201
  corr(VOLATILE,WILDCARD)=0.0616
FOLLOWER n=20000 min=-2739 max=2842 mean=3.98 sd=861.11 signs(+/-/0)=9961/10027/12 p05/50/95=-1414/-4/1424 caps(low/high)=0/0 common=0.7175
  corr(FOLLOWER,WILDCARD)=0.1387
WILDCARD n=20000 min=-4424 max=4304 mean=-1.64 sd=1623.87 signs(+/-/0)=9990/10004/6 p05/50/95=-2724/-2/2699 caps(low/high)=0/0 common=0.1930
index ticks min=557600000000 max=1430400000000 violations=0
isolated BULL fingerprint=ebf316b730ec4f2258cabc2ada2ea824fa7835e6a46bccab1a53d47ce35f7c50 seeds=0..19999
STABLE n=20000 min=-732 max=1215 mean=207.76 sd=305.92 signs(+/-/0)=14886/5096/18 p05/50/95=-301/207/715 caps(low/high)=0/0 common=0.5011
  corr(STABLE,GROWTH)=0.2264
  corr(STABLE,VOLATILE)=0.1652
  corr(STABLE,FOLLOWER)=0.3566
  corr(STABLE,WILDCARD)=0.1169
GROWTH n=20000 min=-1558 max=2147 mean=352.10 sd=612.51 signs(+/-/0)=14143/5844/13 p05/50/95=-655/350/1372 caps(low/high)=0/0 common=0.4433
  corr(GROWTH,VOLATILE)=0.1391
  corr(GROWTH,FOLLOWER)=0.3199
  corr(GROWTH,WILDCARD)=0.0887
VOLATILE n=20000 min=-1965 max=2548 mean=300.12 sd=795.75 signs(+/-/0)=12765/7224/11 p05/50/95=-1015/296/1622 caps(low/high)=0/0 common=0.3075
  corr(VOLATILE,FOLLOWER)=0.2254
  corr(VOLATILE,WILDCARD)=0.0600
FOLLOWER n=20000 min=-1327 max=2189 mean=416.50 sd=559.28 signs(+/-/0)=15275/4706/19 p05/50/95=-515/418/1339 caps(low/high)=0/0 common=0.7179
  corr(FOLLOWER,WILDCARD)=0.1544
WILDCARD n=20000 min=-2583 max=3164 mean=272.32 sd=1041.71 signs(+/-/0)=12100/7895/5 p05/50/95=-1462/274/2004 caps(low/high)=0/0 common=0.2207
index ticks min=741700000000 max=1316400000000 violations=0
isolated BEAR fingerprint=7c7f11557cd6dcdc51d6c9e81776ad43e8703abb29aa245b0dd3e3dafe457117 seeds=0..19999
STABLE n=20000 min=-1296 max=917 mean=-209.47 sd=354.05 signs(+/-/0)=5749/14236/15 p05/50/95=-798/-212/380 caps(low/high)=0/0 common=0.4994
  corr(STABLE,GROWTH)=0.2215
  corr(STABLE,VOLATILE)=0.1519
  corr(STABLE,FOLLOWER)=0.3630
  corr(STABLE,WILDCARD)=0.1157
GROWTH n=20000 min=-2484 max=1811 mean=-353.35 sd=706.42 signs(+/-/0)=6370/13623/7 p05/50/95=-1529/-348/811 caps(low/high)=0/0 common=0.4385
  corr(GROWTH,VOLATILE)=0.1313
  corr(GROWTH,FOLLOWER)=0.3109
  corr(GROWTH,WILDCARD)=0.0948
VOLATILE n=20000 min=-2887 max=2323 mean=-307.03 sd=914.99 signs(+/-/0)=7544/12450/6 p05/50/95=-1822/-316/1222 caps(low/high)=0/0 common=0.3034
  corr(VOLATILE,FOLLOWER)=0.2169
  corr(VOLATILE,WILDCARD)=0.0627
FOLLOWER n=20000 min=-2443 max=1657 mean=-414.06 sd=641.19 signs(+/-/0)=5302/14688/10 p05/50/95=-1471/-410/642 caps(low/high)=0/0 common=0.7188
  corr(FOLLOWER,WILDCARD)=0.1499
WILDCARD n=20000 min=-3477 max=2915 mean=-272.51 sd=1199.98 signs(+/-/0)=8229/11765/6 p05/50/95=-2256/-282/1736 caps(low/high)=0/0 common=0.2168
index ticks min=652300000000 max=1291500000000 violations=0
reference signs=true wildcard/stable=3.3628 high/sideways-median=1.7266 bull>bear=true correlations=true
natural fingerprint=44030c16db9dfd0ebd240bc040753e473c6edfd7ab98434e44987285a6e44fa9 seeds=0..999 rounds=21
STABLE n=21000 min=-1966 max=1791 mean=-3.15 sd=416.51 signs(+/-/0)=10734/10247/19 p05/50/95=-709/11/660 caps(low/high)=0/0 common=0.4192
  event n=3873 mean=-15.92 sd=495.26 p05/50/95=-849/-3/769; no event n=17127 mean=-0.26 sd=396.48 p05/50/95=-661/13/634
  corr(STABLE,GROWTH)=0.3894
  corr(STABLE,VOLATILE)=0.2706
  corr(STABLE,FOLLOWER)=0.5043
  corr(STABLE,WILDCARD)=0.1842
GROWTH n=21000 min=-4182 max=3148 mean=-0.40 sd=822.59 signs(+/-/0)=10771/10212/17 p05/50/95=-1389/24/1304 caps(low/high)=0/0 common=0.3754
  event n=3873 mean=-27.92 sd=1005.33 p05/50/95=-1737/-17/1594; no event n=17127 mean=5.82 sd=775.19 p05/50/95=-1296/31/1235
  corr(GROWTH,VOLATILE)=0.2363
  corr(GROWTH,FOLLOWER)=0.4576
  corr(GROWTH,WILDCARD)=0.1726
VOLATILE n=21000 min=-4364 max=4531 mean=-4.50 sd=985.50 signs(+/-/0)=10588/10402/10 p05/50/95=-1645/11/1570 caps(low/high)=0/0 common=0.2739
  event n=3873 mean=-54.48 sd=1148.82 p05/50/95=-1976/-28/1816; no event n=17127 mean=6.80 sd=944.29 p05/50/95=-1556/19/1524
  corr(VOLATILE,FOLLOWER)=0.3166
  corr(VOLATILE,WILDCARD)=0.1343
FOLLOWER n=21000 min=-4029 max=2860 mean=11.94 sd=764.24 signs(+/-/0)=10973/10020/7 p05/50/95=-1289/39/1223 caps(low/high)=0/0 common=0.5925
  event n=3873 mean=16.79 sd=883.21 p05/50/95=-1488/40/1424; no event n=17127 mean=10.84 sd=734.67 p05/50/95=-1244/39/1177
  corr(FOLLOWER,WILDCARD)=0.2310
WILDCARD n=21000 min=-5000 max=4467 mean=-2.14 sd=1273.41 signs(+/-/0)=10578/10416/6 p05/50/95=-2109/12/2048 caps(low/high)=1/0 common=0.1813
  event n=3873 mean=-49.84 sd=1488.48 p05/50/95=-2535/-31/2352; no event n=17127 mean=8.65 sd=1219.26 p05/50/95=-1971/19/1982
index ticks min=49455448234 max=8761499986358 violations=0
BULL rounds=4756 events=872/0.1833 expected=1800 bps within=true transitions=[2490, 330, 986, 523, 122, 36] within=true streaks=2266 mean=2.099 max=12
BEAR rounds=3968 events=795/0.2004 expected=2000 bps within=true transitions=[258, 1772, 883, 466, 298, 123] within=true streaks=2196 mean=1.807 max=11
SIDEWAYS rounds=7118 events=1005/0.1412 expected=1400 bps within=true transitions=[1297, 1271, 3326, 752, 134, 70] within=true streaks=3792 mean=1.877 max=12
HIGH_VOLATILITY rounds=2984 events=754/0.2527 expected=2600 bps within=true transitions=[389, 400, 589, 954, 315, 161] within=true streaks=2030 mean=1.470 max=7
CRASH rounds=1068 events=252/0.2360 expected=2200 bps within=true transitions=[23, 136, 118, 183, 163, 389] within=true streaks=905 mean=1.180 max=4
RECOVERY rounds=1106 events=195/0.1763 expected=1800 bps within=true transitions=[299, 59, 216, 106, 36, 327] within=true streaks=779 mean=1.420 max=7
unique return sequence signatures=1000/1000
```

</details>

### Performance and remaining validation

Windows 11 Pro x64 with Intel Core Ultra 9 285K, Temurin 21.0.12+8 and a 384 MiB Java heap cap ran 2,000 warm-up rounds and 10,000 individually timed rounds. The default 21-round paths use seeds 0–571, with the last path partial. The timed scope includes prepare, resolve and accept, including established history and transition draws. Summed sample time **0.097462 s**, **102,605 rounds/s**, p50 **0.0084 ms**, p95 **0.0155 ms**. The exploratory 5 ms p95 target held on this host. Startup, session creation and GUI time are outside the timing scope. History reference behavior is tested by eviction and the 21-round paths.

External work pending for this phase includes hosted CI, independent native Linux, macOS and ARM64 validation, plus bundled runtime, signing and release verification. Full gameplay balance awaits the later phases. Phase 04 owns false-news inversion and hidden-source input. Phase 05 owns atomic market and payout integration plus zero-return presentation. Phase 08 owns the finite numerical limit policy for endless mode.
