# Sync Architecture Diagrams

## Database Schema ERD

### Frontend (SQLDelight) Schema

```mermaid
erDiagram
    recipes {
        TEXT local_id PK
        TEXT name
    }

    ingredients {
        TEXT local_id PK
        TEXT name
    }

    recipe_ingredients {
        TEXT recipe_id PK,FK
        TEXT ingredient_id PK,FK
        TEXT quantity_id FK
    }

    units {
        TEXT local_id PK
        TEXT name
        TEXT symbol
        TEXT measurement_type
        REAL base_conversion_factor
    }

    quantities {
        TEXT local_id PK
        REAL amount
        TEXT unit_id FK
    }

    sync_info {
        TEXT entity_id PK
        TEXT entity_type
        INTEGER server_id
        INTEGER local_version
        INTEGER last_modified
        TEXT checksum
        TEXT sync_status
        INTEGER is_pinned
    }

    sync_conflicts {
        INTEGER id PK
        TEXT entity_id FK
        TEXT entity_type
        TEXT local_data
        TEXT remote_data
        INTEGER local_timestamp
        INTEGER remote_timestamp
        INTEGER created_at
    }

    recipes ||--o{ recipe_ingredients : "has"
    ingredients ||--o{ recipe_ingredients : "used in"
    recipe_ingredients ||--o| quantities : "measured by"
    quantities }o--|| units : "uses"

    recipes ||--o| sync_info : "tracks"
    ingredients ||--o| sync_info : "tracks"
    units ||--o| sync_info : "tracks"
    quantities ||--o| sync_info : "tracks"

    sync_info ||--o{ sync_conflicts : "may have"
```

### Backend (Spring/JPA) Schema

```mermaid
erDiagram
    recipes {
        BIGINT id PK
        VARCHAR name
        VARCHAR local_id UK
    }

    ingredients {
        BIGINT id PK
        VARCHAR name
        VARCHAR local_id UK
    }

    recipe_ingredients {
        BIGINT recipe_id PK,FK
        BIGINT ingredient_id PK,FK
        BIGINT quantity_id FK
    }

    units {
        BIGINT id PK
        VARCHAR name
        VARCHAR symbol
        VARCHAR measurement_type
        DOUBLE base_conversion_factor
        VARCHAR local_id UK
    }

    quantities {
        BIGINT id PK
        DOUBLE amount
        BIGINT unit_id FK
        VARCHAR local_id UK
    }

    recipes ||--o{ recipe_ingredients : "has"
    ingredients ||--o{ recipe_ingredients : "used in"
    recipe_ingredients ||--o| quantities : "measured by"
    quantities }o--|| units : "uses"
```

## UML Class/Component Diagram

### Core Sync Architecture

```mermaid
classDiagram
    %% Sync Actions (Sealed Interface)
    class SyncAction {
        <<interface>>
    }

    class AutoSync {
        +trigger: SyncTrigger
    }

    class ManualSync {
        <<object>>
    }

    class ResolveConflict {
        +conflictId: String
        +resolution: ConflictResolution
    }

    class RetryFailed {
        <<object>>
    }

    SyncAction <|-- AutoSync
    SyncAction <|-- ManualSync
    SyncAction <|-- ResolveConflict
    SyncAction <|-- RetryFailed

    %% Sync Trigger Enum
    class SyncTrigger {
        <<enumeration>>
        APP_LAUNCH
        APP_RESUME
        TIMER
        NETWORK_CHANGE
    }

    AutoSync --> SyncTrigger

    %% Conflict Resolution
    class ConflictResolution {
        <<interface>>
    }

    class AcceptLocal {
        <<object>>
    }

    class AcceptRemote {
        <<object>>
    }

    class AcceptNewest {
        <<object>>
    }

    ConflictResolution <|-- AcceptLocal
    ConflictResolution <|-- AcceptRemote
    ConflictResolution <|-- AcceptNewest

    ResolveConflict --> ConflictResolution

    %% Core Sync Engine
    class SyncEngine {
        -syncRepository: SyncRepository
        -api: CookingApi
        -conflictResolver: ConflictResolver
        +performSync(): SyncResult
        +resolveConflict(id: String, resolution: ConflictResolution)
        -getDirtyEntities(): List~EntitySyncInfo~
        -markClean(entityId: String)
        -applyRemoteChanges(data: RemoteData)
    }

    %% Sync Repository
    class SyncRepository {
        -database: CookingDatabase
        +getDirtyEntities(): List~EntitySyncInfo~
        +updateSyncInfo(info: EntitySyncInfo)
        +storeConflict(conflict: SyncConflict)
        +getConflicts(): List~SyncConflict~
        +markDirty(entityId: String, type: String)
    }

    %% Background Sync Manager
    class BackgroundSyncManager {
        -syncEngine: SyncEngine
        -connectivity: ConnectivityManager
        +handleSyncAction(action: SyncAction)
    }

    BackgroundSyncManager --> SyncEngine
    BackgroundSyncManager --> SyncAction
    SyncEngine --> SyncRepository

    %% Data Models
    class EntitySyncInfo {
        +localId: String
        +serverId: Long?
        +localVersion: Int
        +lastModified: Long
        +localChecksum: String
        +syncStatus: SyncStatus
        +isPinned: Boolean
    }

    class SyncStatus {
        <<enumeration>>
        CLEAN
        DIRTY
        SYNCING
        CONFLICT
        ERROR
    }

    class SyncConflict {
        +entityId: String
        +entityType: String
        +localData: String
        +remoteData: String
        +localTimestamp: Long
        +remoteTimestamp: Long
    }

    class SyncResult {
        +synced: Int
        +conflicts: Int
        +errors: List~String~
    }

    EntitySyncInfo --> SyncStatus
    SyncEngine --> EntitySyncInfo
    SyncEngine --> SyncConflict
    SyncEngine --> SyncResult

    %% UI Components
    class ConflictResolutionUI {
        -syncEngine: SyncEngine
        +showConflict(conflict: SyncConflict)
        +showConflictList(conflicts: List~SyncConflict~)
    }

    ConflictResolutionUI --> SyncEngine
    ConflictResolutionUI --> SyncConflict

    %% API
    class CookingApi {
        +sync(entities: List~SyncEntity~): SyncResponse
    }

    class SyncEntity {
        +localId: String
        +serverId: Long?
        +type: String
        +data: JsonObject
        +version: Int
        +timestamp: Long
        +checksum: String
    }

    class SyncResponse {
        +results: List~SyncResultItem~
    }

    class SyncResultItem {
        +localId: String
        +serverId: Long
        +accepted: Boolean
        +hasConflict: Boolean
        +remoteData: JsonObject?
        +remoteTimestamp: Long?
    }

    SyncEngine --> CookingApi
    CookingApi --> SyncEntity
    CookingApi --> SyncResponse
    SyncResponse --> SyncResultItem
```

## Sync Flow Sequence Diagram

```mermaid
sequenceDiagram
    participant App
    participant BGSync as BackgroundSyncManager
    participant Engine as SyncEngine
    participant Repo as SyncRepository
    participant API as Backend API
    participant UI as ConflictUI
    participant User

    App->>BGSync: App Launch
    BGSync->>BGSync: Check Connectivity

    alt Is Online
        BGSync->>Engine: performSync()
        Engine->>Repo: getDirtyEntities()
        Repo-->>Engine: List<EntitySyncInfo>

        Engine->>API: POST /sync (dirty entities)
        API-->>Engine: SyncResponse

        loop For each response
            alt No Conflict
                Engine->>Repo: markClean(entityId)
            else Has Conflict & Not Pinned
                alt Remote is Newer
                    Engine->>Repo: applyRemoteChanges()
                else Local is Newer
                    Engine->>Repo: keepLocal()
                end
            else Has Conflict & Is Pinned
                Engine->>Repo: storeConflict()
            end
        end

        alt Has User Conflicts
            Engine->>UI: showConflicts()
            UI->>User: Display Conflict Dialog
            User->>UI: Choose Resolution
            UI->>Engine: resolveConflict(id, resolution)
            Engine->>Repo: applyResolution()

            alt User Chose "Keep Mine"
                Engine->>Repo: setPinned(entityId, true)
            end
        end

        Engine-->>BGSync: SyncResult
    else Is Offline
        BGSync-->>App: Skip Sync
    end
```

## Data Flow Architecture

```mermaid
graph TB
    subgraph "Frontend (Offline-First)"
        FDB[(SQLDelight DB)]
        FSync[Sync Info Table]
        FConflict[Conflicts Table]

        FDB --> FSync
        FDB --> FConflict
    end

    subgraph "Sync Layer"
        SE[SyncEngine]
        CM[ConflictManager]
        SM[SyncMetadata]

        SE --> CM
        SE --> SM
    end

    subgraph "Backend (Source of Truth)"
        BDB[(PostgreSQL)]
        BAPI[REST API]

        BAPI --> BDB
    end

    subgraph "User Actions"
        Create[Create Recipe]
        Update[Update Recipe]
        Delete[Delete Recipe]
    end

    Create --> FDB
    Update --> FDB
    Delete --> FDB

    FDB -->|Mark Dirty| FSync
    FSync -->|Get Dirty| SE

    SE <-->|Sync Request/Response| BAPI

    SE -->|Conflicts| FConflict
    FConflict -->|User Resolution| CM
    CM -->|Apply Resolution| FDB

    style FDB fill:#e1f5fe
    style BDB fill:#fff3e0
    style SE fill:#f3e5f5
```

## State Machine for Entity Sync Status

```mermaid
stateDiagram-v2
    [*] --> CLEAN: Initial State

    CLEAN --> DIRTY: Local Modification

    DIRTY --> SYNCING: Begin Sync

    SYNCING --> CLEAN: Sync Success (No Conflict)
    SYNCING --> CONFLICT: Conflict Detected
    SYNCING --> ERROR: Sync Failed

    CONFLICT --> SYNCING: User Resolution
    CONFLICT --> DIRTY: Timeout/Cancel

    ERROR --> DIRTY: Retry
    ERROR --> SYNCING: Auto-Retry

    DIRTY --> DIRTY: Additional Changes

    note right of CONFLICT
        User must choose:
        - Accept Local
        - Accept Remote
        - Accept Newest
    end note

    note right of ERROR
        Retry with exponential
        backoff strategy
    end note
```

## Component Interaction Overview

```mermaid
graph LR
    subgraph "Frontend Components"
        UI[UI Layer]
        Store[Recipe/Ingredient Stores]
        Sync[SyncEngine]
        Local[(Local DB)]
    end

    subgraph "Backend Components"
        API[REST Controllers]
        Service[Sync Service]
        Remote[(PostgreSQL)]
    end

    UI -->|User Actions| Store
    Store -->|CRUD Operations| Local
    Store -->|Mark Dirty| Sync

    Sync -->|HTTP/Ktor| API
    API -->|Process| Service
    Service -->|Persist| Remote

    Sync -->|Conflicts| UI
    UI -->|Resolution| Sync

    style Local fill:#e3f2fd
    style Remote fill:#fff9c4
    style Sync fill:#fce4ec
```

## Key Design Decisions Visualized

```mermaid
mindmap
  root((Sync Engine))
    Architecture
      Hybrid Approach
        State-based sync
        Minimal event tracking
      Single sync endpoint
      Offline-first design

    Conflict Resolution
      Default: Newest Wins
      User Override Option
        Keep Mine (Pin)
        Accept Theirs
        Accept Newest
      Deferred Resolution

    Sync Triggers
      MVP: App Launch
      Future Options
        App Resume
        Timer-based
        Network Change

    Data Tracking
      Entity-level (MVP)
        Version numbers
        Timestamps
        Checksums
      Field-level (Future)

    Storage
      Frontend
        sync_info table
        sync_conflicts table
      Backend
        local_id mapping
        Standard JPA entities
```

These diagrams show:

1. **ERD**: The database relationships and how sync metadata connects to domain entities
2. **Class Diagram**: The object-oriented structure with sealed interfaces for extensibility
3. **Sequence Diagram**: The sync flow from app launch through conflict resolution
4. **Data Flow**: How data moves between frontend, sync layer, and backend
5. **State Machine**: The lifecycle of entity sync status
6. **Component Overview**: High-level system architecture
7. **Design Decisions**: Mind map of key architectural choices

The architecture prioritizes:
- **Simplicity**: Single sync point, clear state transitions
- **Extensibility**: Sealed interfaces allow adding features later
- **User Control**: Automatic resolution with override capability
- **Offline-First**: Local DB is primary, sync is opportunistic