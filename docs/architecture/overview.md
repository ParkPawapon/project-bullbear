# Foundation architecture

Gradle compiles each layer separately, so the inner modules can be built and tested without libGDX or native libraries. The root `verifyArchitecture` task checks direct project and library dependencies in production configurations. It also checks import statements for prohibited packages in the inner modules and presentation. The import check covers ordinary and static imports. Build configuration and code review cover fully qualified references and custom configurations.

| Layer | Phase 01 responsibility | Future ownership |
|---|---|---|
| `domain` | Three enums for design identities | Market, news, economy, charms, and progression rules |
| `application` | Catalog of immutable identity lists | Use cases, player-visible read models, and rule orchestration |
| `presentation` | One workspace and route-specific placeholder content | Screens, input, charts, animation, and localization |
| `desktop` | CLI, host startup, native window, composition | Platform-specific configuration and launch packaging |

`DesktopLauncher` composes a `GameDesignCatalog` with a `FoundationGame`. The game creates its skin and screen after OpenGL initialization. The screen owns one `Stage`, a `FitViewport`, and a `ShellNavigation` instance. The stage owns the batch it creates. The screen borrows the skin, which owns the color texture and bitmap font. Shutdown removes owned input routing and releases the stage before disposing the skin.

The current OOP evidence is private route state with validated transitions, immutable catalog output, constructor injection, and composition of framework components. The foundation uses inheritance for the libGDX `Game` and `ScreenAdapter` contracts. The enums express fixed identities.

Future investment, shop, bank, and progression use cases belong under their respective responsibilities in `application`. Domain packages can grow around stock, news, charm, economy, and progression concepts. If a future use case needs determinism or persistence, define its required randomness, clock, or save port at that boundary and add a concrete adapter when the requirement is established. A separate infrastructure module can be evaluated at that point.

[Actual module and class diagrams](diagrams.md) · [Architecture decisions](decisions.md)
