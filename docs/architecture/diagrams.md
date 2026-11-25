# Current foundation diagrams

Arrows in the first diagram represent direct Gradle project dependencies. The domain API is visible through the Java Library contract of `application`. The desktop backend is declared only in `desktop`.

```mermaid
flowchart TD
    Desktop["desktop · game.bullbear.desktop"]
    Presentation["presentation · game.bullbear.presentation"]
    Application["application · game.bullbear.application.catalog"]
    Domain["domain · game.bullbear.domain"]
    Desktop --> Presentation
    Desktop --> Application
    Presentation --> Application
    Application --> Domain
```

The launcher constructs the catalog and game. The game owns the screen and skin, and the screen owns its stage and navigation. The screen rebuilds its placeholder content when the selected route changes.

```mermaid
classDiagram
    class DesktopLauncher {
        +main(args)
    }
    class LauncherOptions {
        <<enumeration>>
        INTERACTIVE
        HELP
        SMOKE_TEST
        parse(args)
    }
    class GameDesignCatalog {
        +stockIds() List~StockId~
        +sourceIds() List~NewsSourceId~
        +charmCategories() List~CharmCategory~
    }
    class StockId {
        <<enumeration>>
        STABLE
        GROWTH
        VOLATILE
        FOLLOWER
        WILDCARD
    }
    class NewsSourceId {
        <<enumeration>>
        A
        B
        C
        D
        E
    }
    class CharmCategory {
        <<enumeration>>
        INFORMATION
        SOURCE_SPECIFIC
        CONDITIONAL_MULTIPLIER
    }
    class FoundationGame {
        +create()
        +render()
        +dispose()
        +smokeFrameRendered() boolean
    }
    class FoundationSkin {
        +create() Skin
    }
    class FoundationScreen {
        +show()
        +render(delta)
        +resize(width, height)
        +hide()
        +dispose()
    }
    class ShellNavigation {
        -current Route
        +current() Route
        +navigateTo(route)
        +backToHome()
    }
    class Route {
        <<enumeration>>
        HOME
        NEWS
        STOCKS
        SHOP
        BANK
        +title() String
    }
    Game <|-- FoundationGame
    ScreenAdapter <|-- FoundationScreen
    DesktopLauncher ..> LauncherOptions
    DesktopLauncher ..> FoundationGame : constructs
    DesktopLauncher ..> GameDesignCatalog : constructs
    FoundationGame --> GameDesignCatalog
    FoundationGame *-- FoundationScreen
    FoundationGame ..> FoundationSkin
    FoundationGame *-- Skin
    FoundationSkin ..> PixelTheme
    FoundationSkin ..> Skin : creates
    FoundationScreen --> Skin : borrows
    FoundationScreen *-- Stage
    FoundationScreen *-- ShellNavigation
    ShellNavigation --> Route
    GameDesignCatalog --> StockId
    GameDesignCatalog --> NewsSourceId
    GameDesignCatalog --> CharmCategory
```

The [architecture overview](overview.md) documents future package ownership. These diagrams correspond to the Phase 01 source.
