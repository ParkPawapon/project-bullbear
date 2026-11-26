# Bullbear

Bullbear is a fictional investing roguelite for desktop computers. Phase 01 contains a runnable workspace with News, Stocks, Shop, and Bank placeholders.

ตัวเกมระยะ Foundation เปิดหน้าต่างและสลับหน้าได้ ระบบลงทุนและระบบเล่นจริงอยู่ในเฟสถัดไป

## Choose your device

| Device | Start here |
|---|---|
| Windows PC, x86_64 | [Windows](#windows) |
| Mac with Apple Silicon or Intel | [macOS](#macos) |
| Linux PC, x86_64, including WSLg | [Linux](#linux) |
| Android, iPhone, iPad, Chromebook, or browser | Read the project on GitHub. The runnable game targets desktop Java with OpenGL. |

Windows and Linux ARM64 remain conditional targets. See the [platform notes](docs/foundation/platform-support.md) for observed coverage.

## Requirements

- A Java 21 JDK that matches your device architecture. Set `JAVA_HOME` when using multiple JDKs.
- Git and network access on the first build to download the pinned toolchain. Use the wrapper from the project root.
- A desktop session and OpenGL-compatible driver for the game window. `clean check` runs without graphics.

Clone the repository and run the commands for your device from its root.

```sh
git clone https://github.com/ParkPawapon/project-bullbear.git
cd project-bullbear
```

## Windows

In PowerShell, run these commands from the project root. In Command Prompt, use `gradlew.bat` in place of `.\gradlew.bat`.

```powershell
.\gradlew.bat --version
.\gradlew.bat :desktop:run
.\gradlew.bat clean check
.\gradlew.bat build :desktop:installDist
```

Start the installed distribution with `.\desktop\build\install\bullbear\bin\bullbear.bat`.

## macOS

Install Java 21 for your Mac. `uname -m` reports `arm64` on Apple Silicon and `x86_64` on Intel. Build the archive on the target Mac so the launcher includes `-XstartOnFirstThread`.

```sh
./gradlew :desktop:run
./gradlew clean check
./gradlew build :desktop:installDist
./desktop/build/install/bullbear/bin/bullbear
```

## Linux

Use an X11 or Xwayland session with working OpenGL to open the workspace. The same commands work in a WSLg session with a Linux JDK.

```sh
./gradlew :desktop:run
./gradlew clean check
./gradlew build :desktop:installDist
./desktop/build/install/bullbear/bin/bullbear
```

## Controls and diagnostics

Use Tab and Shift+Tab to move focus, Enter or Space to activate a control, and Escape to return Home from a placeholder.

- Use `--args=--help` with `:desktop:run` to check the launch arguments without graphics.
- Use `--args=--smoke-test` with `:desktop:run` for a real frame and clean exit. A successful run prints `FOUNDATION_SMOKE_OK`. Give cached runs up to 60 seconds.
- Run `spotlessApply` through the wrapper to apply the Java formatter.

## Modules

| Module | Responsibility |
|---|---|
| `domain` | Java identities for the design concepts |
| `application` | Immutable design catalog |
| `presentation` | Scene2D workspace and navigation |
| `desktop` | Native launch and distribution |

[Original design](GAME_DESIGN.md) · [Traceability](docs/design/traceability.md) · [Architecture](docs/architecture/overview.md) · [Verification](docs/foundation/verification.md)

## CI archives and contributions

Choose the artifact for your OS in a successful [main workflow run](https://github.com/ParkPawapon/project-bullbear/actions/workflows/ci.yml). Each download contains a Gradle ZIP and its SHA-256 file. Extract the artifact bundle and then the distribution ZIP. Run the launcher inside `bin/` using Java 21.

See the [contribution policy](CONTRIBUTING.md) for branch codes and reviews and the [security policy](SECURITY.md) for private reports.

## Phase 01.1 engineering tools

Run `clean check` for JUnit, Spotless, module boundaries, JaCoCo coverage and SpotBugs. Run `:desktop:cyclonedxDirectBom` to generate the desktop runtime SBOM. Run `:desktop:archiveAppImage` to build a native candidate with Java bundled. Run from the target operating system and JVM architecture.

Use `--args=--thai-preview` with `:desktop:run` to view the shaped Thai sample. Use `--args=--diagnostics` to print logical and framebuffer dimensions and sampled frame timing. The default workspace remains English.

See the [engineering runbook](docs/engineering/phase-01.1-runbook.md) for local settings, crash reports, release candidates, and dependency maintenance. [Verification evidence](docs/engineering/phase-01.1-evidence.md)
