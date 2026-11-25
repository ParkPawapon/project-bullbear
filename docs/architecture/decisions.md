# Foundation decisions

| Decision | Reason and consequence |
|---|---|
| Adopt the existing `project-bullbear` repository identity | Use `game.bullbear` as the development namespace and `bullbear` as the ASCII launcher name. The game title and branding remain product decisions. |
| Java 21, Gradle, libGDX and LWJGL3 | The inner layer supports ordinary Java tests and the outer layer provides a native desktop window with a 2D rendering and input stack. |
| One screen with a small route model | Four unavailable destinations share one layout and one owned stage. Dedicated screens can follow when they need distinct behavior. |
| Fixed logical viewport and runtime colors | `FitViewport` keeps the 1280 × 720 logical aspect ratio as the window changes. A generated solid texture and libGDX bitmap font are enough for the foundation. |
| English shell, runtime graphics smoke by opt-in | The default font covers the English UI and the smoke path renders with real native graphics. Thai rendering and default headless checks have separate acceptance criteria. |
| Install locally using the Gradle application plugin | The generated launcher works from another directory, and still requires Java. Native app images, installer signing, and publication belong to a release phase. |
