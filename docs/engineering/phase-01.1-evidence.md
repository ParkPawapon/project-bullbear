# Phase 01.1 evidence

## Baseline on 2026-11-26

- Source `1a0c451911bbc795591f33af0d69aa08907f7823`, clean `main` tracking `origin/main`, empty staged diff, `gradlew` mode `100755`.
- Native Windows 11 build 26300 x86_64. Temurin JDK 21.0.12+8 x86_64 and `jpackage` 21.0.12. Gradle 8.14.8, libGDX 1.14.2 and LWJGL 3.3.3.
- `GAME_DESIGN.md` SHA-256 `b604fb3ea4ae95ac3942f52fdd089cf4d06dfc39d1a9820288ff96b20bfc06d0`.
- Wrapper binary distribution and JAR hashes match Gradle's published SHA-256 values for 8.14.8. [Gradle checksums](https://gradle.org/release-checksums/)
- Baseline `gradlew.bat --no-daemon clean check build :desktop:installDist` exited 0, 36 executed tasks, 31 passing tests, about 10.9 seconds on this host.
- Baseline GitHub run passed at the same revision. Read-only API inspection confirmed rulesets 28093959/28093960 with required CI, CodeQL, PRs, linear history and force-push/deletion protections. [Baseline run](https://github.com/ParkPawapon/project-bullbear/actions/runs/45667316060)

## Scope

The four feature panels retain their Phase 01 placeholder behavior. The candidate manifests identify the source as `working-tree` at the baseline revision. Only the published baseline has hosted CI evidence.

## Windows gate results

- `gradlew.bat --no-daemon --no-build-cache clean check build :desktop:installDist :desktop:archiveAppImage :desktop:cyclonedxDirectBom` exited 0 in Gradle's reported 27 seconds, with all 57 tasks executed. JUnit reports show **49 passed, 0 failed, 0 skipped**. Spotless, the module boundary check, JaCoCo floors and the eight high-confidence SpotBugs tasks passed.
- A forced second `:desktop:distZip --no-build-cache --rerun-tasks` produced the same conventional ZIP SHA-256, `150322603cb992ad5c82fc5bab7139b5e05a948389b65ae19267c3b15b86eb09`. Native `jpackage` archive reproducibility across builds or hosts is outside this proof.
- An isolated task-output-cache experiment ran `clean :desktop:test` twice. The first build took 8.59 s with 16 tasks executed. The second took 5.79 s with six tasks restored from cache, including `:desktop:test`. The separate `check --configuration-cache` runs stored and reused a configuration-cache entry. The graphics smoke task also stored a configuration-cache entry. Opt in per supported command.
- `:desktop:run --configuration-cache --args=--smoke-test` rendered and printed `FOUNDATION_SMOKE_OK`. The smoke mode switches into all four panels twice through Scene2D listeners, returning Home between switches. The installed launcher from a separate working directory containing spaces and Thai characters exited 0 and printed the same marker.
- In the native Windows window, clicking News opened its panel, Escape returned Home, and maximizing the window preserved the layout and the Bank hit area. The Exit button closed it. The startup diagnostics sample was `fps=0 frameDeltaMs=0.00 logical=1280x720 framebuffer=1280x720`. It was the initial sample before consecutive rendered frames. The default smoke output had no diagnostic sample.
- The Thai preview was visually checked in the Windows window. All ten fixed examples displayed their marks, including above and below combinations, with no visibly clipped or missing glyphs. Escape closed the preview. The renderer uses Pillow 12.3.0 with libraqm 0.10.5 and HarfBuzz 14.2.1, and the native screen draws the resulting texture. This check covers the exercised logical size and static labels. The preview does not exercise wrapping or text entry. [Exact strings and generator](../../tools/render_thai_preview.py)

| Module | JUnit tests | JaCoCo lines | JaCoCo branches |
|---|---:|---:|---:|
| `domain` | 3 | 16/16 (100%) | No branch counter |
| `application` | 6 | 8/8 (100%) | No branch counter |
| `presentation` | 18 | 41/357 (11.48%) | 8/92 (8.70%) |
| `desktop` | 22 | 173/261 (66.28%) | 54/95 (56.84%) |

Module reports are under `build/test-results/test`, `build/reports/jacoco/test` and `build/reports/spotbugs` in each subproject. The coverage figures come from the JUnit tasks. Native smoke covers GL paths separately.

## Security and release evidence

- JaCoCo 0.8.14, SpotBugs Gradle plugin 6.5.13 and engine 4.10.5, CycloneDX Gradle plugin 3.6.1, Gitleaks 8.30.1 and actionlint 1.7.12. The Wrapper distribution and JAR remained pinned. The four strict module locks cover compile/runtime, test compile/runtime, JaCoCo agent/reporting and SpotBugs. Build-tool inputs use catalog pins and strict artifact verification, including Gradle metadata and POMs. Maven Central and the Gradle Plugin Portal are the configured sources.
- CycloneDX's build graph initially resolved Guava 33.7.1-jre, flagged by OSV under `GHSA-xxph-c9ww-hj94`. Pinning the patched 33.7.2-jre resolved it. The live OSV API on 2026-11-26 returned all 221 Maven-coordinate results with zero advisory matches. No exceptions were applied. The API response does not identify a database snapshot. JDK modules, native binaries embedded in other artifacts and Actions require separate vendor coverage.
- Strict resolution uses 411 SHA-256 entries. All 239 added entries were compared against approved upstream repositories. Of them, 113 had upstream SHA-256 sidecars. For the other 126, the downloaded artifact matched its upstream SHA-1 sidecar and supplied the locally computed SHA-256. Fresh WSL resolution exposed additional BOM and parent metadata, which was verified before being pinned. The strict build then passed with empty dependency caches.
- Gitleaks scanned 79 reachable commits and the scoped working tree with redaction and reported zero leaks. An isolated synthetic fixture was rejected with exit 1. GitHub's read-only API reported secret scanning and push protection enabled, automatic security-update PRs disabled and repository auto-merge enabled. The checked-in Dependabot version-update schedule awaits hosted registration.
- `actionlint` passed locally. All eight unique Action pins in the workflows matched their stated upstream tags through GitHub's commits API. Workflow tokens default to `contents: read`, CodeQL has `security-events: write` for its job, and the optional attestation job has OIDC and attestation write permissions. Fork PR jobs use read-only Gradle cache access. The release candidate workflow is manual on protected `main` and uploads candidate artifacts.
- CycloneDX JSON conformed to the official 1.6 schema on both candidates. Each lists 15 components plus the primary desktop component and 16 dependency nodes. `candidate.json` maps 58 shipped JARs to the SBOM with checksums and classifiers. The three internal submodule entries have unspecified project licensing. External Maven components have declared terms or upstream notices. The bundled Temurin runtime has `runtime/legal`, and the app-image includes `THIRD_PARTY_NOTICES.md` and font notices. Validate any redistributable native/source obligations before public release.
- Noto Sans Thai Looped v2.000 has SIL OFL 1.1. The official release archive SHA-256 is `d82d9a0d4f7b453d6eda8b3e10be680b0265138f610e91e4f5e887222f8bdb79`. The pinned regular TTF SHA-256 is `d941d72ac9043b0a376fcc36f1dd538abfec690259915e89f68ca48cc5772726`. The shipped preview PNG SHA-256 is `8e5b7b05209fd0777abf6f17c954ee27e80dcc2cf334faea963472e807c66b1e`. [Upstream release](https://github.com/notofonts/thai/releases/tag/NotoSansThaiLooped-v2.000)
- Windows candidate `desktop/build/candidate/bullbear-0.1.0-windows-x64.zip`, 69,454,126 bytes, SHA-256 `cb3f8732b95ed1bf0743aa9c58932e583c4df42e7e8ff2b992aa3ea8d03da329`. The `.exe` extracted from that archive rendered, reported `Bullbear 0.1.0` on a separate version check, and exited 0 with `JAVA_HOME` unset and no Java available via `PATH`. The runtime reports Java 21.0.12 and contains `runtime/bin/server/jvm.dll`.
- Linux candidate `desktop/build/candidates/linux-x64/bullbear-0.1.0-linux-x64.tar.gz`, 72,400,735 bytes, SHA-256 `d8dcaa3b0d86efdbff779ab87522252986433f199818166934bfb1895b20d047`. The installed and app-image launchers each printed `FOUNDATION_SMOKE_OK` with WSLg. The app-image ran with `JAVA_HOME` unset and an empty `PATH` from a separate working directory. Its `lib/runtime/release` reports Java 21.0.12.
- `candidate_manifest.py create` and `verify` passed on both targets. An altered BOM in a temporary copy caused verification to exit 1. Candidate files include `bom.cdx.json`, `candidate.json` and `SHA256SUMS`. Both app-images are unsigned. Hosted dispatch, attestation, signing, notarization, installer builds and publication await the owner.

## Requirement register

| ID | Initial | Action | Final status | Evidence | Remaining scope |
|---|---|---|---|---|---|
| B01 | Existing | Added shared tool and plugin versions | `IMPLEMENTED_VERIFIED` | `gradle/libs.versions.toml`, wrapper build | — |
| B02 | Partial | Extended strict locks to test, coverage and SpotBugs graphs | `IMPLEMENTED_VERIFIED` | Four module locks, lock mismatch experiment | Build plugin artifacts use checksum verification |
| B03 | Existing | Pinned added jars and transitive metadata | `IMPLEMENTED_VERIFIED` | `gradle/verification-metadata.xml`, strict build, wrong-checksum fixture | Continue upstream verification for changes |
| B04 | Existing | Retained reproducible archive order and timestamps | `VERIFIED_EXISTING` | Identical Windows `distZip` SHA-256 across two builds | Native archive reproducibility outside proof |
| B05 | Absent | Enabled task output cache and measured reuse | `IMPLEMENTED_VERIFIED` | Two `clean :desktop:test` runs | Host-specific timings |
| B06 | Incompatible in baseline task | Declared architecture task inputs and checked supported graphs | `IMPLEMENTED_VERIFIED` | `check --configuration-cache` stored and reused | Opt in per supported command |
| C01 | Unit suite and smoke | Added filesystem and boundary integration tests, expanded smoke | `IMPLEMENTED_VERIFIED` | 49 JUnit tests, `FOUNDATION_SMOKE_OK` | Native tests stay outside default JUnit |
| C02 | Existing hosted workflow | Added scanners to stable required gate | `PARTIAL_VERIFIED` | `.github/workflows/ci.yml`, actionlint, baseline run | Changed workflow awaiting hosted PR |
| C03 | Existing archive job | Added quality and advisory artifacts | `PARTIAL_VERIFIED` | Local reports present, workflow lint passed | Hosted upload of new reports pending |
| C04 | Existing | Reviewed read-only PR and protected push cache policy | `VERIFIED_EXISTING` | Baseline run and `setup-gradle` config | Changed workflow awaiting hosted run |
| C05 | CI delivery only | Added protected manual native candidate workflow | `PARTIAL_VERIFIED` | Candidate workflow lint, local candidate bundles | Hosted dispatch and owner publication |
| C06 | Single Gradle version | Wired CLI resource, app-image and dispatch version check | `IMPLEMENTED_VERIFIED` | `:desktop:printAppVersion`, launcher and candidate metadata all `0.1.0` | Future release tag is owner action |
| C07 | Existing ZIP digest | Added exact native payload manifest and strict SHA-256 check | `IMPLEMENTED_VERIFIED` | `candidate.json`, `SHA256SUMS`, altered-BOM rejection | Hosted generation pending |
| C08 | Existing four runners | Retained matrix and added native candidate jobs | `PARTIAL_VERIFIED` | Windows and WSLg local runs, local actionlint | Current hosted jobs and macOS runtime pending |
| Q01 | Absent | Added JaCoCo reports and coverage policy | `IMPLEMENTED_VERIFIED` | XML counters and coverage floor experiment | GL paths checked separately |
| Q02 | Absent | Enforced high-confidence SpotBugs Main and Test runs | `IMPLEMENTED_VERIFIED` | Eight passing analysis tasks with XML | Policy scope is high-confidence findings |
| Q03 | Existing | Reviewed line endings, UTF-8 and build formatting | `VERIFIED_EXISTING` | `.editorconfig`, `git diff --check`, Spotless | Editor behavior depends on EditorConfig support |
| Q04 | Absent | Reviewed Error Prone overlap and JDK integration | `DEFERRED_JUSTIFIED` | SpotBugs and CodeQL cover current analysis needs | Reconsider for a concrete compile-time bug class and verify Java 21 integration |
| Q05 | Absent | Reviewed SonarQube operating requirements | `DEFERRED_JUSTIFIED` | Local reports and stable CI gate available | Reconsider with an approved instance, ownership and history need |
| Q06 | Absent | Reviewed JMH fit for the current shell | `DEFERRED_JUSTIFIED` | Opt-in frame diagnostics available | Reconsider when simulation code offers a measured JVM hotspot |
| S01 | Absent repo schedule | Added weekly Gradle and Actions version updates | `CONFIGURED_NOT_RUN` | `.github/dependabot.yml`, read-only API status | Confirm hosted schedule and dependency graph ingestion |
| S02 | Existing | Uses the same strict verification as B03 | `IMPLEMENTED_VERIFIED` | B03 metadata and negative fixture | Shared implementation with B03 |
| S03 | Existing review and audit | Added OSV gate and patched the flagged Guava build graph | `IMPLEMENTED_VERIFIED` | `audit_dependencies.py`, OSV response and report | Vendor coverage differs for JDK, Actions and bundled natives |
| S04 | Existing hosted protection | Added redacted history scan and safe fixture | `IMPLEMENTED_VERIFIED` | Gitleaks results, read-only GitHub settings | Hosted workflow scan pending |
| S05 | Existing active rulesets | Verified CI, CodeQL, review and limited bypass via API | `VERIFIED_EXISTING` | Rulesets 28093959/28093960 | Repo settings were inspected read-only |
| S06 | Existing scoped permissions | Preserved read-only defaults and scoped attestation | `IMPLEMENTED_VERIFIED` | Workflow review and actionlint | Hosted OIDC permission execution pending |
| S07 | Existing notices | Added font/runtime terms and checked candidate layout | `PARTIAL_VERIFIED` | `THIRD_PARTY_NOTICES.md`, shipped licenses and JDK legal directory | Owner confirms project licensing and native redistribution obligations |
| S08 | Absent | Produced runtime CycloneDX BOM and exact classifier mapping | `IMPLEMENTED_VERIFIED` | 1.6 schema, candidate manifest and 58 JAR mappings | JDK modules and vendored native internals use separate inventories |
| S09 | Existing pinned infrastructure | Verified upstream pins and added checksum/scan gates | `IMPLEMENTED_VERIFIED` | Strict build and reviewed action SHAs, dependency and tool checksums | Cross-platform hosted follow-up |
| S10 | Absent | Added gated optional artifact attestation | `CONFIGURED_NOT_RUN` | `.github/workflows/candidate.yml`, verification command in runbook | Protected hosted run and attestation verification |
| R01 | Existing classpath resources | Added preview bitmap with fixed classpath path and owner | `IMPLEMENTED_VERIFIED` | Preview opened, packaged resources found | Single static asset and existing generated textures |
| R02 | Existing disposal paths | Added reverse cleanup and exercised repeated switching | `IMPLEMENTED_VERIFIED` | `OwnedResourcesTest`, real smoke and preview Exit | Extended soak outside exercised scope |
| R03 | Existing routes | Exercised all four placeholders and Home twice in smoke | `IMPLEMENTED_VERIFIED` | `ShellNavigation` tests and `FOUNDATION_SMOKE_OK` | No gameplay routes added |
| R04 | Existing input | Checked active stage ownership and native coordinate mapping | `IMPLEMENTED_VERIFIED` | Scene2D listeners and Windows clicks, Escape and Exit | Platform/DPI-specific checks tracked below |
| R05 | Absent | Added bounded versioned desktop preferences | `IMPLEMENTED_VERIFIED` | `SettingsStoreTest`, Windows runtime logs | No settings editor in shell |
| R06 | Absent | Added rotating local logs | `IMPLEMENTED_VERIFIED` | `DesktopDiagnosticsTest`, Windows shutdown log | No log upload |
| R07 | Partial | Added recoverable settings fallback and fatal exit reporting | `IMPLEMENTED_VERIFIED` | Settings fixtures and startup-boundary check | JVM or driver fatal error reports are platform-managed |
| R08 | Existing | Retained fit viewport and exercised native resize | `VERIFIED_EXISTING` | Windows maximized Bank click and Home layout | Additional platform sizes remain |
| R09 | Implicit | Made logical HiDPI mode explicit and reported framebuffer size | `PARTIAL_VERIFIED` | Native sample and default/maximized Windows input | Windows 125/150/200% and macOS Retina untested |
| R10 | English literals | Moved shell keys into base English and Thai bundles | `IMPLEMENTED_VERIFIED` | `ShellStringsTest` and English native window | Thai is an opt-in technical preview |
| R11 | Latin bitmap | Added shaped Thai preview with verified OFL | `PARTIAL_VERIFIED` | Ten Windows visual cases, pinned Noto and renderer inputs | Cross-host visual checks, wrapping and dynamic Thai |
| R12 | Backend audio disabled | Added mute and volume preferences, retained silent boot | `IMPLEMENTED_VERIFIED` | Settings bounds tests and real silent smoke | Physical playback deferred until licensed assets exist |
| R13 | Absent | Added per-user settings outside install root and atomic writes | `IMPLEMENTED_VERIFIED` | Invalid path/write cases and native startup from separate directory | Original file retained on rejected writes |
| R14 | Absent | Implemented format version with future-version preservation | `IMPLEMENTED_VERIFIED` | `SettingsStoreTest` unknown-version fixture | Settings schema fixture only |
| R15 | Absent | Added seeded JDK random factory and repeatability check | `IMPLEMENTED_VERIFIED` | `SimulationRandomTest`, seeded `java.util.Random` | Future independent simulation streams when needed |
| R16 | Absent | Added bounded local crash record with stack types and frames | `IMPLEMENTED_VERIFIED` | `DesktopDiagnosticsTest` and startup-boundary check | Driver/JVM dumps kept separate |
| R17 | Absent | Added opt-in bounded FPS, frame time and dimensions logging | `IMPLEMENTED_VERIFIED` | Windows enabled and default smoke logs | Initial sample is separate from steady-state profiling |
| P01 | Java-dependent distribution | Added native `jpackage` app-image and archive | `IMPLEMENTED_VERIFIED` | `:desktop:archiveAppImage` and Windows native launch | Signed native installers and macOS launch pending |
| P02 | Absent | Bundled Temurin runtime and tested isolated launch | `IMPLEMENTED_VERIFIED` | `runtime/release`, Windows smoke without system Java | Native proof scoped to exercised hosts |

## Phase 01.1 platform evidence

Windows is an interactive native host. Linux checks ran on Ubuntu 26.04 x64 under WSL2/WSLg (`6.18.33.2-microsoft-standard-WSL2`). Both used Temurin 21.0.12+8 and `jpackage` 21.0.12. Linux ran `./gradlew --no-daemon --no-build-cache clean check build :desktop:installDist :desktop:archiveAppImage :desktop:cyclonedxDirectBom` from a copied working tree using empty Gradle dependency and task caches. Gradle reported 41 seconds, 56 tasks executed and 1 up-to-date.

| Platform / environment | Dependencies | Compile | Tests | Dist archive | App-image | Native installer | Bundled Java launch | GUI smoke | Thai preview | Dedicated HiDPI | Audio playback |
|---|---|---|---|---|---|---|---|---|---|---|---|
| Windows x64, native | PASS | PASS | PASS | PASS | PASS | NOT_RUN | PASS | PASS | PASS, fixed labels | NOT_RUN | N/A, silent shell |
| Linux x64, WSLg | PASS | PASS | PASS | PASS | PASS | NOT_RUN | PASS | PASS | NOT_RUN | NOT_RUN | N/A, silent shell |
| Linux x64, standalone | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | N/A, silent shell |
| macOS arm64, current source | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | N/A, silent shell |
| macOS x64, current source | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | N/A, silent shell |
| Windows arm64 | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | N/A, silent shell |
| Linux arm64 | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | NOT_RUN | N/A, silent shell |

The prior GitHub run covers the published Phase 01 baseline only. Current GitHub jobs are pending on all runner targets. Dedicated HiDPI tests include Windows 125/150/200% and macOS Retina. See the [historical platform and GUI record](../foundation/verification.md).

## Acceptance gates and final audit

| Gate | Result | Checks |
|---|---|---|
| G0 Baseline | PASS | T01 ran all 31 baseline tests. Baseline commit, wrapper provenance and Git index were recorded. |
| G1 Build | PASS | T02–T04 rejected a forbidden dependency, missing lock entry and wrong SHA-256 in isolated copies. T05–T07 passed the Windows archive rebuild, task cache and configuration-cache reuse. Strict builds ran on both hosts. |
| G2 Quality and security | PASS locally | T08–T14 verified integration test execution, rejection with a raised coverage floor, SpotBugs, synthetic secret detection, live OSV, SBOM validation and packaged notices. T32 passed workflow lint. Current hosted execution is pending. |
| G3 Runtime | PASS locally | T15–T19 passed settings validation, preservation and seeded RNG checks. T20 removed the Thai preview from an isolated JAR. Native startup returned 1 and produced a bounded local crash report. T21 tested reverse cleanup and failure aggregation. T22–T25 passed repeated navigation, native resize/click checks, locale parity and visual Thai static labels on Windows. T26 exercised silent boot with audio assets unavailable. T27–T28 verified local crash logs and opt-in diagnostics. |
| G4 Packaging | PASS locally | T29–T31 passed launch from a different directory, app-image launch without external Java and rejection of an altered candidate BOM. Installers, signing and hosted attestation remain pending. |
| G5 Handoff | PASS | T33–T34 verified the original design hash, unchanged HEAD and empty staged diff, wrapper mode, scoped files and phase limits. |

The 49 dispositions are 32 `IMPLEMENTED_VERIFIED`, 5 `VERIFIED_EXISTING`, 7 `PARTIAL_VERIFIED`, 2 `CONFIGURED_NOT_RUN`, and 3 `DEFERRED_JUSTIFIED`. One control satisfies both B03 and S02. At the local handoff, the staged index was empty. `GAME_DESIGN.md` retains SHA-256 `b604fb3ea4ae95ac3942f52fdd089cf4d06dfc39d1a9820288ff96b20bfc06d0`. The existing local `.vscode` directory and Wrapper timestamp comment were preserved. The user-approved Wrapper URL repair restored 8.14.8 to match its existing checksum. The local verification pass ended before any commit or push. Remote settings, installers and gameplay were outside its scope.

**Phase 01.1 status** `COMPLETE_LOCAL_WITH_EXTERNAL_PENDING`. Rerun commands and owner actions are in the [engineering runbook](phase-01.1-runbook.md).

## GitHub integration follow-up

Phase 01.1 was later split into coded branches. The following pull requests passed the current protected CI gate before rebase merge. Platform smoke and publication limitations in the local record still apply.

- DE002 [5 commits and merged PR](https://github.com/ParkPawapon/project-bullbear/pull/11) · [GitHub Actions](https://github.com/ParkPawapon/project-bullbear/actions/runs/45784496435)
- FE003 [4 commits and merged PR](https://github.com/ParkPawapon/project-bullbear/pull/12) · [GitHub Actions](https://github.com/ParkPawapon/project-bullbear/actions/runs/45784802883)
- CI004 [4 commits and merged PR](https://github.com/ParkPawapon/project-bullbear/pull/13) · [GitHub Actions](https://github.com/ParkPawapon/project-bullbear/actions/runs/45785123586)

## Dependency maintenance follow-up

On 2026-11-27 the maintained build passed strict clean checks and distribution assembly on Windows x64 and a fresh Gradle dependency cache on Ubuntu x64 under WSL2. Windows reported 78 tests with zero failures or skips, reused the opt-in configuration cache on the second check, and ran the installed shell to `FOUNDATION_SMOKE_OK`. The action upgrades passed protected GitHub CI on the four configured runners. The manual candidate workflow was checked with Actionlint. [Dependency provenance](../foundation/dependencies.md#november-2026-dependency-maintenance) · [Action upgrade CI](https://github.com/ParkPawapon/project-bullbear/actions/runs/45872919931)

On 2026-11-28 Gradle 9.9.3 passed strict clean checks and package assembly on Windows and WSL2. Windows ran 78 tests with zero failures or skips. The Windows and WSLg installed launchers rendered the shell. The Windows app-image launched with bundled Java and no external Java on `PATH`. Windows stored and reused the configuration cache for `check`. A second conventional ZIP build matched SHA-256 `5d60c1ca0043c303d868b51b0c92032b31674084b3bb00f0fb8768ef88842782`. [Wrapper verification and compatibility notes](../foundation/dependencies.md#gradle-wrapper-993)
