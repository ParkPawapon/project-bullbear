# Foundation architecture

Gradle compiles each layer separately, so the inner modules can be built and tested without libGDX or native libraries. The root `verifyArchitecture` task checks direct project and library dependencies in production configurations. It also checks import statements for prohibited packages in the inner modules and presentation. The import check covers ordinary and static imports. Build configuration and code review cover fully qualified references and custom configurations.

| Layer | Current responsibility | Future ownership |
|---|---|---|
| `domain` | Design identities, core run aggregate, immutable player view and deterministic market simulation | News, settlement, economy and charm algorithms |
| `application` | Identity catalog, seeded random factory, in-memory game session and headless market owner | Playable use cases and coordination of resolvers |
| `presentation` | One workspace and route-specific placeholder content | Screens, input, charts, animation, and localization |
| `desktop` | CLI, host startup, native window, composition, local settings and logs, app-image packaging | Platform adapters for future use cases |

`DesktopLauncher` reads platform settings and starts local diagnostic logging before it composes a `GameDesignCatalog` with a `FoundationGame`. The game creates its skin and screen after OpenGL initialization. The screen owns one `Stage`, a `FitViewport`, and a `ShellNavigation` instance. The stage owns the batch it creates. The screen borrows the skin, which owns the color texture and bitmap font. Shutdown removes owned input routing and releases the stage before disposing the skin. The optional Thai preview owns a separate stage and shaped texture. The game releases acquired resources in reverse order when initialization fails or the window closes.

The current OOP evidence is private route state with validated transitions, immutable catalog output, constructor injection, and composition of framework components. The foundation uses inheritance for the libGDX `Game` and `ScreenAdapter` contracts. The enums express fixed identities.

Phase 02 groups structural types under `domain.game` and keeps the stock, news and charm enum contracts. `GameRun` owns balances and transitions. `GameSession` in `application.game` gives callers immutable snapshots and reserves aggregate access for trusted coordination. It is exercised by headless tests. Desktop continues to compose the catalog and shell. Future resolvers can receive a `RandomGenerator` from `SimulationRandom.seeded` and supply trusted inputs at the tested boundaries.

Phase 03 freezes a round's mood, noise and optional event before resolving five returns in `domain.market`. `MarketSession` in `application.market` owns accepted market state for headless callers. `GameRun` retains ownership of money and the run lifecycle. A future coordinator connects market acceptance to settlement.

[Module and shell diagrams](diagrams.md) · [Domain model and sequence](domain-model.md) · [Market simulation](market-simulation.md) · [Run state machine](game-state-machine.md) · [Architecture decisions](decisions.md)
