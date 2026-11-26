# Phase 02 domain model

`GameRun` is the only mutable owner of a run. A single coordinator thread applies commands. The run owns the wallet, cumulative bank, special currency, inventory, shop and current round. `GameRules` and `GameCatalog` are immutable supplied configuration. The existing `GameDesignCatalog` continues to expose the same enum identities for the desktop shell.

`GameSession` starts a run from supplied rules and returns immutable `GameSnapshot` values. Only application code in the session package can access its active aggregate. The desktop composition root uses the existing shell setup and does not create a session. Trusted domain inputs include a complete `RoundEntry` and an already-computed `Investment.Settlement`. Market and economy algorithms can construct these inputs in subsequent phases.

```mermaid
classDiagram
    class GameSession {
        +start(runId) GameSnapshot
        +view() Optional
    }
    class GameRun {
        +enter(roundEntry)
        +deposit(key, amount)
        +commit(key, allocation)
        +settle(settlement)
        +snapshot() GameSnapshot
    }
    class GameRules
    class GameCatalog
    class StockId
    class NewsSourceId
    class SourceDefinition["GameCatalog.SourceDefinition"]
    class CharmDefinition
    class OwnedCharm["CharmDefinition.Owned"]
    class RoundKey
    class RoundEntry
    class NewsClaim
    class Shop
    class Commitment["Investment.Commitment"]
    class Allocation["Investment.Allocation"]
    class Settlement["Investment.Settlement"]
    class GameSnapshot
    GameSession *-- "0..1" GameRun
    GameRun --> GameRules
    GameRun --> GameCatalog
    GameCatalog --> "5" StockId
    GameCatalog *-- "5" SourceDefinition
    SourceDefinition --> NewsSourceId
    GameCatalog *-- "*" CharmDefinition
    GameRun *-- "0..capacity" OwnedCharm
    OwnedCharm --> CharmDefinition : definition ID
    GameRun *-- RoundKey
    GameRun *-- "0..1" RoundEntry
    RoundEntry *-- "*" NewsClaim
    GameRun *-- Shop : fixed configured slots
    GameRun *-- "0..1" Commitment
    Commitment *-- Allocation
    Settlement --> Commitment
    GameRun ..> Settlement : accepts trusted input
    GameRun ..> GameSnapshot : projects
    GameSnapshot --> "3..5" NewsSourceId : visible sources
    GameSnapshot --> "*" NewsClaim : visible claims only
```

Allocation maps and movement maps use stock enum order. Catalog sources and projected source sets use source enum order. Claims and owned charms retain supplied and acquisition order respectively. Shop slot IDs are one-based positions. A round entry keeps its full claim list internally. The projection strips hidden claims and exposes the immutable investment result only after settlement.

## Structural round sequence

```mermaid
sequenceDiagram
    participant F as Test fixture
    participant S as GameSession
    participant R as GameRun
    F->>S: construct with supplied rules and catalog
    F->>S: start with supplied run ID
    S->>R: start
    S-->>F: ROUND_ENTRY snapshot
    F->>R: enter with supplied credit and claims
    Note over R: PLANNING
    F->>R: commit current round key and allocation
    F->>R: settle with fixture movements, returns, and reward
    F->>S: view
    S-->>F: POST_RESULT snapshot
    opt Optional protected deposit
        F->>R: deposit using current round key
    end
    F->>R: continueRound for round 1 or 2
    Note over R: ROUND_ENTRY
```

The application integration test reaches the aggregate through its trusted package. The desktop owns the separate scene navigation. See the [actual transitions](game-state-machine.md) and [module boundaries](overview.md).
