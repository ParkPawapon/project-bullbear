# Phase 01 verification

Verification date 2026-11-25 (Asia/Bangkok).

## Scope and environment

The foundation has four Gradle modules, a read-only design catalog, a desktop shell, and an installed distribution. `GAME_DESIGN.md` was copied byte-for-byte from the original supplied alongside the brief. The repository name informed the working title and namespace. The shell follows the brief's written visual direction and uses English placeholders.

The original gameplay, balance, art, localization, and release packaging decisions remain deferred.

- Windows 11 build 26300 x86_64, with an interactive graphics session.
- Ubuntu 26.04 LTS x86_64, WSL2 kernel `6.18.33.2-microsoft-standard-WSL2`, with a WSLg X11 display for smoke runs.
- Eclipse Temurin 21.0.12+8 x86_64, Gradle 8.14.8, libGDX 1.14.2 and LWJGL 3.3.3.

## Executed checks

| Gate | Status | Evidence |
|---|---|---|
| G01 Repository and inputs | PASS | `git rev-parse --show-toplevel` identified the open project. The original and repository design both hash to `b604fb3ea4ae95ac3942f52fdd089cf4d06dfc39d1a9820288ff96b20bfc06d0` (SHA-256). No approved images were present in the workspace. |
| G02 Wrapper | PASS | Official Gradle task generated the scripts, JAR and properties. The ZIP and JAR matched Gradle's published SHA-256. `gradlew.bat --version` exited 0. |
| G03 Module graph | PASS | `gradlew.bat projects` lists `domain`, `application`, `presentation`, and `desktop`. `verifyArchitecture` checks the direct production dependencies. |
| G04 Java target | PASS | `javap -verbose` reported class file version 65 in each module. The toolchain and compile release are 21. |
| G05 Inner layers | PASS | `:domain:compileJava :application:compileJava :domain:test :application:test` passed. Clean reports show 3 domain and 5 application tests. `jdeps -s` shows only `java.base` in domain and `java.base` plus domain in application. |
| G06 Outer logic | PASS | Clean reports show 14 navigation and 9 launcher-option tests. There were 31 tests total with zero failures, errors or skips. |
| G07 Quality gates | PASS | `gradlew.bat clean check` executed all Java formatting, tests and architecture checks. A temporary init script added an invalid `domain → application` runtime edge and the gate rejected it with exit 1. |
| G08 Build and distribution | PASS | `gradlew.bat build :desktop:installDist` generated ZIP, TAR and the installed layout. The installed layout contains 58 JARs, both launch scripts, the notices and license files. A forced Windows rebuild repeated ZIP SHA-256 `d375d5c4941c53638024248614e84ca50e03144248707452fdb3cf129f10bdfe`. |
| G09 Graphics launch | PASS | `gradlew.bat :desktop:run --args=--smoke-test` printed `FOUNDATION_SMOKE_OK` and exited 0 on Windows. Interactive checks are below. |
| G10 Distribution launch | PASS | The absolute path to `bullbear.bat --smoke-test` resolved resources and natives from a separate temporary directory on Windows. It printed the marker and exited 0. The normal installed launcher also opened at Home. |
| G11 README and commands | PASS | Run, check, build, format and install tasks were exercised. Help exited 0. An invalid flag printed usage and exited 2 before opening a window. |
| G12 UML and OOP | PASS | The module arrows and implemented class relationships were checked against the Gradle declarations and source. |
| G13 Design traceability | PASS | GD-01 through GD-31 are mapped to the catalog or future ownership. The record preserves Thai copy, open decisions and proposed differentiation. |
| G14 Scope | PASS | Source review found identity metadata, desktop startup and placeholder navigation only. Gameplay remains deferred. |
| G15 Repository hygiene | PASS | `git diff --cached --check` passed against a temporary index of the full proposed tree. The generated Wrapper JAR is the only binary in that tree. The POSIX wrapper has Git mode `100755`. |
| G16 Dependency integrity | PASS | Four generated module lockfiles, strict SHA-256 verification and official repository checksum comparison. Details in [dependency provenance](dependencies.md). |
| G17 Platform disclosure | PASS | [Platform support](platform-support.md) distinguishes observed runs from target hosts. |
| G18 CI configuration | PASS | YAML parsed locally, runner labels and action pins checked. GitHub Actions execution NOT RUN. |

## Windows interactive smoke

| Checks | Status | Observation |
|---|---|---|
| SM-01, SM-02 | PASS | Native window showed the workspace and Foundation status. |
| SM-03 to SM-07 | PASS | Clicked News, Stocks, Shop and Bank. Each showed its unavailable status, and Back / Home returned to the four controls. |
| SM-08 | PASS | Maximized and restored the original window size. Layout remained centered and usable. An exact 960 × 540 client size and dedicated HiDPI check were NOT RUN. |
| SM-09 | PASS | Minimize and restore returned to the live workspace. |
| SM-10 | PASS | Tab and Shift+Tab moved focus in both directions. Enter and Space activated a route. Escape returned Home. |
| SM-11, SM-12 | PASS | Exit and the native Close control each terminated the corresponding process with exit 0. |
| SM-13 | PASS | A new interactive launch started at Home. |
| SM-14, SM-15 | PASS | Installed launcher rendered and exited from a separate working directory. |

Window inspection exposed a missing em-dash glyph and a Shift+Tab event-order issue. The shell now draws the separator with the theme texture and tracks Shift in input-event order. Both paths were rechecked in the window.

## Platform evidence

| Target | Build and tests | Gradle graphics | Installed graphics |
|---|---|---|---|
| Windows x86_64 | PASS | PASS | PASS |
| Linux x86_64, Ubuntu 26.04 under WSLg | PASS | PASS | PASS |
| macOS arm64 | NOT RUN | NOT RUN | NOT RUN |
| macOS x86_64 | NOT RUN | NOT RUN | NOT RUN |
| Windows arm64 and Linux arm64 | NOT RUN | NOT RUN | NOT RUN |

On Linux, a temporary checkout and an empty `GRADLE_USER_HOME` verified first resolution in strict mode. `env -u DISPLAY -u WAYLAND_DISPLAY ./gradlew --no-daemon clean check` passed all 31 tests. `./gradlew --no-daemon build :desktop:installDist` and the installed launcher's display-free `--help` exited 0. The Gradle smoke command and the installed `bullbear --smoke-test` from a separate working directory each printed `FOUNDATION_SMOKE_OK` and exited 0 under a 60-second timeout.

## Decisions and open risks

- The Unix wrapper was staged to retain its executable bit on the Windows host. The other files are working-tree additions.
- The initial fresh Linux build exposed a missing JUnit BOM metadata checksum from the plugin classpath. The two additional BOM metadata checksums matched the official repository sidecars. A second build from an empty cache passed in strict mode.
- The shell uses English until Thai shaping and layout can be tested with licensed assets. The repo's working name informed the namespace and display name.
- Native installer creation, release signing, and remote CI execution remain unverified.

## Phase status

**COMPLETE.** Phase 01 gates passed on the checked hosts. Further gameplay and product work requires explicit authorization.

## GitHub hardening follow-up

The earlier gate table records the initial Phase 01 sign-off. See the [GitHub Actions history](https://github.com/ParkPawapon/project-bullbear/actions/workflows/ci.yml) for subsequent hosted runs and their deliverables.

The 2026-11-26 dependency audit found a high-severity advisory affecting the formatter's transitive Guava version. The formatter now resolves Guava 33.7.2-jre. The refreshed strict build ran all 31 tests. The 13 new or changed SHA-256 pins matched Maven Central. A repeat OSV advisory query reported zero matches for the 74 Maven coordinates in the updated manifest.
