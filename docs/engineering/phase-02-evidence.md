# Phase 02 verification evidence

Verification date 2026-11-26 (Asia/Bangkok). **Status** `COMPLETE_LOCAL_WITH_EXTERNAL_PENDING`.

## Baseline and scope

- Baseline `c4809e5613caafa87f542566c0dd8ed8442ed367` on `main`. Staged diff empty. The pre-existing Wrapper timestamp comment and local `.vscode/` settings were kept. The Unix Wrapper remains executable.
- Windows 11 x64 and Ubuntu 26.04 x64 under WSL2/WSLg kernel `6.18.33.2-microsoft-standard-WSL2`. Both used Temurin 21.0.12+8 and checked-in Gradle 8.14.8. libGDX 1.14.2 and LWJGL 3.3.3 remain in the desktop graph.
- Baseline Windows `gradlew.bat --no-build-cache clean check build :desktop:installDist` exited 0 in 12 seconds. All 54 tasks executed and JUnit recorded 49 passing tests, zero failures, errors or skips. The prior published Phase 01.1 run is historical for this phase. [Baseline hosted CI](https://github.com/ParkPawapon/project-bullbear/actions/runs/45786064969)
- Phase 02 adds a headless structural model under `domain.game` and a session boundary under `application.game`. The existing catalog, deterministic random seam, desktop composition and presentation code retain their original behavior. All structural scenario inputs live in test source.

## Executed gates

| Gate | Result | Evidence |
|---|---|---|
| G-00 Baseline | PASS | Wrapper and Java version checked, module and source inventory inspected, 49 baseline tests passed. |
| G-01 Architecture | PASS | `verifyArchitecture` and `jdeps -s` passed. Published domain bytecode requires `java.base`. Application requires domain and `java.base`. |
| G-02 Structural correctness | PASS | 29 new domain/application tests cover value validation, atomic mutation, duplicate/stale actions, hidden claims, session isolation, seven-stage traversal and endless paths. |
| G-03 Quality | PASS | Strict `clean check` on Windows and WSL completed with Spotless, eight high-confidence SpotBugs tasks, JaCoCo and the unchanged coverage policy. |
| G-04 Supply chain | PASS locally | Dependency delta is zero. Strict resolution passed without writing locks or verification metadata. OSV returned results for 221 Maven coordinates with zero advisory matches. Gitleaks 8.30.1 reported zero leaks on the scoped Phase 02 worktree files with redaction. |
| G-05 Build and runtime | PASS locally | Windows and WSL build and `:desktop:installDist` passed. Real graphical smoke printed `FOUNDATION_SMOKE_OK` on both hosts. Windows app-image used its bundled Java. |
| G-06 Traceability | PASS | Original design hash matched before and after. Class, state and sequence diagrams, decision log, design addendum and scenario mapping use implemented contracts. |
| G-07 Hygiene | PASS | `git diff --check` passed. During local verification the Git index remained empty, HEAD was at baseline, and GitHub received no changes. Windows archive inspection found production classes and no source design, prompts, private settings, crash logs or core test fixtures. |

OSV used the live API on the verification date. Its coverage is Maven-coordinate advisories, with vendor coverage of the JDK, Actions and embedded native binaries tracked separately. Scanner and workflow configuration were preserved.

### Build and graphical checks

- Windows `gradlew.bat --console=plain --no-build-cache clean check build :desktop:installDist :desktop:archiveAppImage` exited 0 in 24 seconds, all 57 tasks executed. `:desktop:run --args=--smoke-test` rendered and exited normally. The installed launcher and Windows app-image also rendered and exited from a separate working directory. The app-image reported bundled Java 21.0.12 with `JAVA_HOME` unset and Java absent from `PATH`.
- WSL `env -u DISPLAY -u WAYLAND_DISPLAY ./gradlew --no-daemon --console=plain --no-build-cache clean check build :desktop:installDist` exited 0 in 16 seconds, 53 tasks executed and 1 up-to-date. The installed launcher rendered with WSLg from a separate working directory and printed `FOUNDATION_SMOKE_OK`.
- The existing smoke opens News, Stocks, Shop and Bank twice through their Scene2D listeners and returns Home between each visit. The tests keep the placeholder model. Phase 02 did not change the language preview, renderer or audio.

| Windows artifact | Bytes | SHA-256 |
|---|---:|---|
| `desktop/build/distributions/bullbear-0.1.0.zip` | 13,793,424 | `100393e7158097040c402a6e3f723eb2e1e9822fa58ca0e416f2dd9faf04b56b` |
| `desktop/build/candidate/bullbear-0.1.0-windows-x64.zip` | 69,494,640 | `ba6fe865ec1436dd1e30b3ba7d425e3d5a70a5ddc7728e773915874d420c8b41` |

### JUnit and coverage

Both Windows and WSL report **78 tests, zero failures, errors or skips**. Coverage below is from the final Windows JaCoCo XML reports.

| Module | Tests | Lines | Branches |
|---|---:|---:|---:|
| domain | 30 | 383/383 (100%) | 148/152 (97.37%) |
| application | 8 | 25/25 (100%) | 8/8 (100%) |
| presentation | 18 | 41/357 (11.48%) | 8/92 (8.70%) |
| desktop | 22 | 173/261 (66.28%) | 54/95 (56.84%) |

Each module writes JUnit XML to `build/test-results/test`, coverage XML and HTML to `build/reports/jacoco/test`, and static analysis XML to `build/reports/spotbugs`. The Phase 01.1 thresholds remain in force. Native smoke covers the shell paths outside headless coverage.

### Platform evidence

| Environment | Build and tests | Installed GUI | Bundled runtime GUI | Remaining scope |
|---|---|---|---|---|
| Windows x64 native | PASS | PASS | PASS | Dedicated HiDPI, dynamic Thai and physical audio |
| Ubuntu x64 WSLg | PASS, headless | PASS | NOT_RUN for Phase 02 | Independent native Linux desktop, bundle rerun |
| Native Linux x64 and Windows/Linux ARM64 | NOT_RUN | NOT_RUN | NOT_RUN | Target-specific environments |
| macOS arm64 and Intel | NOT_RUN for Phase 02 | NOT_RUN | NOT_RUN | Previous hosted matrix is Phase 01.1 evidence |
| GitHub Actions, signing, attestation and installers | NOT_RUN in the local pass | — | — | See the hosted integration follow-up. Signing, attestation and installers are separate release work |

## Requirement and scenario mapping

| Coverage | Actual model and checks | Boundary |
|---|---|---|
| V-01–10, CAT-01–09, T-001–015 | Typed currency and IDs, checked operations, exact source set and increasing goals. `ValueContractsTest` and endless paths in `GameRunTest`. | Stock definitions are the fixed `StockId` enum. Source probability and impact are supplied in basis points. |
| ECO-01–14, INV-01–08, T-016–033 | Deposit conservation and overflow, wallet-only credits, nonbinding draft, exact budget at commit and frozen stake. `GameRunTest` and `ValueContractsTest`. | A Java map cannot represent duplicate stock keys. Future parsing must reject duplicates before creating the map. |
| NEWS-01–13, CH-01–08, SHOP-01–07, T-034–049 | Trusted claims retained, safe snapshots, monotonic visibility, unique bounded ownership, fixed offers and revision-checked replacement. `GameRunTest` and `ValueContractsTest`. | Case T-040 varies actual hidden claims. Truth values are deferred. Purchases and generated offers are future work. |
| INV-09–20, FSM-01–24, FAIL-01–07, T-050–079 | Keyed settlement, validation for all five stocks, once-only payout and credit, rejection atomicity, equality and final deposits, campaign victory, endless success/failure/finish and restart. `ValueContractsTest`, `GameRunTest`, and `GameSessionTest`. | Configuration faults and representational limits are explicit. Unexpected programming failures propagate. Stage assessment is an atomic `closeStage` command. |
| APP-01–08, A-01–12, T-080–090 | Existing catalog and module contracts, single-session owner, immutable views and deterministic scenario equality without runtime effects. `GameSessionTest`, `GameRunTest`, existing catalog tests and `verifyArchitecture`. | Presentation receives no active run handle. Trusted coordinator code remains unwired in desktop. |
| T-091–096 | Existing 49 tests retained, installed shell and Windows app-image smoke, production archive inspection, no dependency delta, source hash and documentation audit. | The hosted integration follow-up below records the later CI. External environments and release steps listed above remain pending. |

[Original requirements mapping](../design/traceability.md#phase-02-structural-mapping) · [Adopted decisions](../architecture/decisions.md#phase-02-core-domain-decisions) · [Design proposals and handoff](../design/phase-02-game-design-addendum.md)

## Checkpoint

| Package | Status | Exit evidence |
|---|---|---|
| WP-0 | VERIFIED | Baseline inventory, tests and source hash |
| WP-A | VERIFIED | Explicit catalogs, typed values and rule validation |
| WP-B | VERIFIED | Protected balance and once-only credit |
| WP-C | VERIFIED | Hidden-claim projection, shop capacity and outcome shape |
| WP-D | VERIFIED | Keyed transitions, overflow/retry atomicity and seven-stage traversal |
| WP-E | VERIFIED | Headless session and installed shell/native regression |
| WP-F | VERIFIED | Actual diagrams, D-01–18, GD-01–31 and design addendum |
| WP-G | VERIFIED locally | Final build, tests, safety scan, packages and Git audit |

The Phase 03 handoff starts at `StockId`, `Investment.Commitment`, `Investment.Settlement` and `SimulationRandom.seeded`. Market generation and price data remain future work.

`GAME_DESIGN.md` SHA-256 before and after is `b604fb3ea4ae95ac3942f52fdd089cf4d06dfc39d1a9820288ff96b20bfc06d0`.

## GitHub integration follow-up

The four core pull requests below passed the protected GitHub Actions gate on Windows, Linux, macOS arm64 and macOS Intel before rebase merge. The Linux workflow exercised the installed shell under Xvfb. These checks complement the local platform evidence. Native macOS GUI, signing, attestation and installer verification remain separate.

- BE002 [3 commits and merged PR](https://github.com/ParkPawapon/project-bullbear/pull/20) · [GitHub Actions](https://github.com/ParkPawapon/project-bullbear/actions/runs/45859677567)
- BE003 [4 commits and merged PR](https://github.com/ParkPawapon/project-bullbear/pull/21) · [GitHub Actions](https://github.com/ParkPawapon/project-bullbear/actions/runs/45860250103)
- BE004 [4 commits and merged PR](https://github.com/ParkPawapon/project-bullbear/pull/22) · [GitHub Actions](https://github.com/ParkPawapon/project-bullbear/actions/runs/45860724004)
- BE005 [2 commits and merged PR](https://github.com/ParkPawapon/project-bullbear/pull/23) · [GitHub Actions](https://github.com/ParkPawapon/project-bullbear/actions/runs/45861314771)
