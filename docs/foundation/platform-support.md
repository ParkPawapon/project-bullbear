# Platform support

The build targets Java 21 and uses libGDX 1.14.2 with LWJGL 3.3.3. A supported GLFW/OpenGL environment is required for interactive runs. [libGDX desktop launch documentation](https://libgdx.com/wiki/start/import-and-running)

| Platform | Intended support | Evidence |
|---|---|---|
| Windows x86_64 | Primary | Build, native window, interaction, and installed launcher PASS on Windows 11 build 26300. |
| Linux x86_64 | Primary | Clean-cache build, both graphics launches, and headless checks PASS on Ubuntu 26.04 under WSLg. Standalone Linux installations and driver combinations remain untested. |
| macOS arm64 | Primary, with a native JDK | Local host build and graphics execution were not exercised. CI uses `macos-15`. |
| macOS x86_64 | Primary where Intel Java and OS support are available | Local host build and graphics execution were not exercised. CI uses `macos-15-intel`. |
| Windows arm64 and Linux arm64 | Conditional | NOT RUN. Check matching JDK and backend native artifacts before claiming support. |

GitHub Actions builds and tests on Windows Server 2025 x86_64, Ubuntu 24.04 x86_64, macOS 15 arm64, and macOS 15 Intel x86_64. Linux CI opens the installed distribution with Xvfb and Mesa. macOS CI checks the first-thread flag in the installed launcher. See each run for actual results. [GitHub-hosted runner labels](https://docs.github.com/en/actions/reference/runners/github-hosted-runners)

See the original Phase 01 commands and results in the [verification record](verification.md) and the current results in the [Phase 01.1 evidence](../engineering/phase-01.1-evidence.md).

## Launch and distribution

- Use the commands in the [README](../../README.md). The desktop renderer requires a display session. Unit tests and `--help` can run without one.
- Build the distribution on the target OS. On macOS, the Gradle run task and locally generated launcher include `-XstartOnFirstThread`. Use a Java runtime that matches the host and native library architecture.
- On Linux, the default GLFW backend needs an X11 display such as Xorg, Xwayland, or Xvfb with a working OpenGL driver. The CI workflow uses Mesa software rendering in Xvfb.
- The Gradle distribution contains scripts and JARs and requires a compatible external Java runtime. Phase 01.1 adds unsigned `jpackage` app-images with a bundled runtime. See the [candidate runbook](../engineering/phase-01.1-runbook.md#unsigned-native-candidates).
- The shell defaults to English. The separate Thai technical preview uses pre-shaped labels. Dynamic text, wrapping and host-specific visual results are tracked in the [Phase 01.1 evidence](../engineering/phase-01.1-evidence.md).

| Symptom | Investigation |
|---|---|
| Unsupported class version | Check `java -version` and the Wrapper launcher and daemon JVMs. |
| Native library failed to load | Match JVM architecture to OS and inspect the resolved runtime classifiers. |
| macOS first-thread error | Confirm the run path was generated on macOS and contains the first-thread option. |
| GLFW display unavailable | Check the session and OpenGL driver. On Linux check `DISPLAY` and the X server or Xwayland. |
| UI clipped or blurry | Check client sizing and DPI scaling. The logical canvas uses a 16:9 viewport. |
