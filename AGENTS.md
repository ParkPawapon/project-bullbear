# Repository instructions

## Scope

- Bullbear is a Java 21 desktop game foundation using Gradle and libGDX LWJGL3.
- Keep current work within Phase 01 until the user explicitly authorizes a later phase.
- Preserve the original `GAME_DESIGN.md` bytes. Read `docs/design/traceability.md` for deferred rules and open design questions.
- Keep placeholders visibly unavailable. Avoid invented game values, temporary game rules, or production art.

## Architecture

- Keep `domain` pure Java and `application` independent of graphics, files, and networking.
- `application` exposes domain identities via the immutable catalog. `presentation` owns route state, drawing, and input.
- `desktop` owns native startup and the composition root. See `docs/architecture/overview.md`.
- Prefer small classes with explicit ownership, constructor injection, and immutable data where appropriate.
- Initialize GL resources after context creation and dispose each exactly once in its owner.
- Preserve logical aspect ratio and verify real rendering when changing the shell.

## Build and evidence

- Use the checked-in Gradle Wrapper with a Java 21 JDK. Pin versions in `gradle/libs.versions.toml`.
- Keep Maven Central and the Gradle Plugin Portal as the build's approved sources.
- Regenerate and review locks and verification metadata for dependency updates. See `docs/foundation/dependencies.md`.
- Run `./gradlew clean check` or `.\gradlew.bat clean check` for source changes. Keep default tests graphics-free.
- Use `:desktop:run --args=--smoke-test` for graphics changes and `:desktop:installDist` for packaging changes.
- Record actual results and platform gaps in `docs/foundation/verification.md`.
- Preserve user changes. Commit, push, publish, or change remote state only by explicit request.
