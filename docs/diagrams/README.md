# Architecture Diagrams

This directory contains Mermaid diagrams documenting the cooking app's architecture, data flows, and user interactions.

## Directory Structure

```
docs/diagrams/
├── frontend/          Frontend diagrams (Compose Multiplatform, SQLDelight)
│   ├── frontend-architecture.mermaid
│   ├── frontend-schema.mermaid
│   ├── recipe-management-flow.mermaid
│   ├── ingredient-assignment-flow.mermaid
│   ├── edit-mode-states.mermaid
│   ├── navigation-overview.mermaid
│   ├── user-scenarios.mermaid
│   └── user-flows.mermaid (visual index)
└── backend/           Backend diagrams (Spring Boot, PostgreSQL)
    └── schema.mermaid
```

## Frontend Diagrams

### Architecture & Data

- **[frontend-architecture.mermaid](frontend/frontend-architecture.mermaid)** - Complete UML class diagram showing:
  - Domain layer (Recipe, Ingredient models)
  - Data layer (Repository pattern with SQLDelight)
  - Presentation layer (Store pattern with StateFlow)
  - UI layer (Compose screens)
  - Extensive annotations on patterns, threading, testing strategies

- **[frontend-schema.mermaid](frontend/frontend-schema.mermaid)** - SQLDelight local database schema:
  - Entity-relationship diagram
  - recipes, ingredients, recipe_ingredients tables
  - UUID generation strategy, indices
  - Migration notes and future plans

### User Flows

- **[recipe-management-flow.mermaid](frontend/recipe-management-flow.mermaid)** - Complete recipe CRUD workflow:
  - Create new recipes
  - View recipe details (read-only mode)
  - Edit recipes (edit mode with auto-save)
  - Delete recipes with confirmation

- **[ingredient-assignment-flow.mermaid](frontend/ingredient-assignment-flow.mermaid)** - Ingredient search and assignment:
  - Debounced search (300ms delay, immediate for blank queries)
  - Checkbox interactions (assign/remove)
  - Create new ingredients inline from search query
  - Dialog state management

- **[edit-mode-states.mermaid](frontend/edit-mode-states.mermaid)** - State diagram for recipe detail screen:
  - View mode vs Edit mode transitions
  - When edit mode persists vs resets
  - Action flows (EnterEditMode, ExitEditMode, etc.)
  - Notes on dual store coordination

- **[navigation-overview.mermaid](frontend/navigation-overview.mermaid)** - High-level app navigation structure:
  - Tab navigation (Recipe tab, Ingredient tab)
  - State-based navigation pattern
  - Dialog overlays
  - Conditional screen rendering

- **[user-scenarios.mermaid](frontend/user-scenarios.mermaid)** - End-to-end user journeys:
  - Scenario 1: Create Recipe with Ingredients
  - Scenario 2: Quick View Recipe
  - Scenario 3: Edit Existing Recipe

### Index

- **[user-flows.mermaid](frontend/user-flows.mermaid)** - Visual index diagram showing all user flow documentation

## Backend Diagrams

- **[schema.mermaid](backend/schema.mermaid)** - PostgreSQL database schema (Spring Boot backend)

## Architecture Patterns

### Store Pattern (MVI-inspired)
- **Unidirectional data flow**: UI → Action → Store → State → UI
- **StateFlow**: Reactive state management with Kotlin Flows
- **Actions**: Sealed interfaces for type-safe user intents
- **States**: Immutable data classes representing UI state

### Repository Pattern
- **Abstraction**: Interfaces allow swapping data sources (local/remote)
- **SQLDelight**: Type-safe local database with reactive queries
- **Flow-based**: Database changes automatically update UI via Flow

### Navigation Pattern
- **State-based**: No traditional nav controllers
- **Conditional rendering**: `selectedRecipeId` determines which screen shows
- **Edit mode**: Boolean flag in state drives view vs edit composables

## Key Implementation Notes

### Edit Mode Behavior
- `ViewRecipeDetail` ALWAYS resets `isEditMode = false` (regression fix)
- Edit mode persists when opening/closing ingredient dialog
- Edit mode does NOT persist when navigating between recipes
- Test coverage: `RecipeDetailNavigationTest.kt`

### Search Optimization
- 300ms debounce on typed queries (reduces database load)
- Blank queries execute immediately (no delay for clearing search)
- Case-insensitive search using SQLite `LOWER()` function

### Dual Store Coordination
- `AssignIngredientsDialog` uses both `RecipeStore` and `IngredientStore`
- `RecipeStore`: Manages assignments (recipe_ingredients junction table)
- `IngredientStore`: Manages search and ingredient CRUD
- State sync via callbacks passed to dialog

## Viewing Diagrams

These diagrams use Mermaid syntax and can be viewed in:
- GitHub (renders automatically)
- IDEs with Mermaid plugins (VS Code, IntelliJ)
- [Mermaid Live Editor](https://mermaid.live)
- Documentation sites (GitBook, MkDocs, etc.)

## Living Documents

These diagrams are intended as **living documentation** that evolves with the codebase:
- Update diagrams when making architectural changes
- Add annotations explaining "why" not just "what"
- Document regression fixes and important decisions
- Keep implementation notes accurate

Last Updated: 2026-01-03
