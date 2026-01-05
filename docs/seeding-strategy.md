# Database Seeding Strategy

## Overview

The app uses a **conditional seeding** approach that separates production data from development test data.

## Architecture Pattern: Simple Conditional Seeding

We chose **Option A** (simple conditional seeding) over:
- ❌ **Complex inheritance hierarchies** (overkill for current scale)
- ❌ **Data files (YAML/JSON)** (adds bundle size, parsing overhead, loses type safety)
- ✅ **Interface-based composition** (could scale to this later if needed)

**Rationale**: YAGNI (You Aren't Gonna Need It) - start simple, refactor if complexity grows.

## Implementation

### 1. Environment Detection (`BuildConfig`)

Cross-platform environment flag using Kotlin Multiplatform expect/actual:

```kotlin
expect object BuildConfig {
    val isDebug: Boolean
}
```

**Platform Implementations:**

- **Android**: Uses generated `BuildConfig.DEBUG` from build types
- **iOS**: Defaults to `true` (TODO: configure via Xcode build settings)
- **Desktop**: Checks system property `-Dapp.environment=production` or env var `APP_ENVIRONMENT=production`

**Usage:**
```kotlin
if (BuildConfig.isDebug) {
    // Development build
} else {
    // Production build
}
```

### 2. Seeding Logic (`DatabaseSeeder`)

Single `object` with two public methods:

#### `seedProduction(unitRepository: UnitRepository)`
- **Runs in**: All builds (debug + production)
- **Seeds**: Essential reference data
  - 17 common measurement units (g, kg, oz, lb, ml, L, tsp, tbsp, cup, etc.)
- **Idempotent**: Only seeds if tables are empty

#### `seedDevelopment(recipeRepository, ingredientRepository, unitRepository)`
- **Runs in**: Debug builds only
- **Seeds**:
  - Production data (via `seedProduction()`)
  - 20 common test ingredients (flour, sugar, chicken, etc.)
  - 10 sample recipes with randomly assigned ingredients
- **Idempotent**: Only seeds if tables are empty

### 3. Initialization (`ServiceLocator.init()`)

Called on app startup:

```kotlin
initScope.launch {
    if (BuildConfig.isDebug) {
        DatabaseSeeder.seedDevelopment(recipeRepo, ingredientRepo, unitRepo)
    } else {
        DatabaseSeeder.seedProduction(unitRepo)
    }
}
```

## Data Organization

### Production Data (Always Seeded)

**Units** (`commonUnits` in DatabaseSeeder.kt)
- Weight: gram (base), kilogram, ounce, pound
- Volume: milliliter (base), liter, teaspoon, tablespoon, cup, pint, quart, gallon, fluid ounce
- Count: whole (base), piece, dozen

Stored in code for:
- ✅ Type safety (compile-time validation)
- ✅ Version control (track changes in git)
- ✅ Fast loading (no parsing overhead)

### Development Data (Debug Only)

**Ingredients** (`testIngredients` in DatabaseSeeder.kt)
- 20 common pantry staples (flour, sugar, eggs, chicken, vegetables, etc.)

**Recipes** (`testRecipes` in DatabaseSeeder.kt)
- 10 sample recipes (cookies, pasta, salads, soups, etc.)
- Each assigned 2-4 random ingredients on seeding

## Usage Examples

### Running in Different Modes

**Debug Mode (default):**
```bash
./gradlew :frontend:desktop:run
# Seeds production + development data
```

**Production Mode:**
```bash
./gradlew :frontend:desktop:run -Dapp.environment=production
# Seeds only production data (units)
```

**Android:**
- Debug builds: Automatically seeds dev data
- Release builds: Only seeds production data

### Adding New Seed Data

#### Adding Production Data (e.g., new units)

1. Edit `DatabaseSeeder.kt` → `commonUnits` list
2. Add new unit with proper conversion factor:
```kotlin
Unit(
    localId = "unit-${UUID.generateUUID()}",
    name = "milliliter",
    symbol = "ml",
    measurementType = MeasurementType.VOLUME,
    baseConversionFactor = 1.0  // Base unit
)
```

3. **Important**: This will only apply to fresh installs or after deleting database
4. For existing users, you'd need a migration + seeder to backfill

#### Adding Development Data (e.g., more test recipes)

1. Edit `DatabaseSeeder.kt` → `testRecipes` list
2. Add new recipe:
```kotlin
Recipe(localId = "", name = "Chocolate Cake")
```

3. Restart app in debug mode - new recipe will be seeded

## Future Considerations

### When to Refactor to More Complex Pattern

Consider refactoring when:
- ❌ **10+ different seeder types** → Use interface-based composition
- ❌ **100+ seed records** → Consider data files or server-driven seeding
- ❌ **Different seed sets for QA/staging/prod** → Add more environment types
- ❌ **Frequently changing seed data** → Consider external JSON files or admin panel

### Data Files Approach (Not Implemented)

If seed data grows significantly, could migrate to:

```
resources/
  seed/
    production/
      units.json
    development/
      ingredients.json
      recipes.json
```

**Pros**: Easy to edit, can hot-reload in dev
**Cons**: Parsing overhead, type safety loss, larger APK/IPA

**Recommendation**: Only if seed data exceeds 50+ records per type.

## Testing

Seeding is tested indirectly via:
- `RecipeDetailNavigationTest` - Verifies recipes can be created
- `RecipeStoreTest` - Tests data persistence
- Manual testing - Run app and verify data appears

For explicit seeder tests, add:
```kotlin
@Test
fun `seedProduction creates 17 units`() = runTest {
    val unitRepo = TestUnitRepository()
    DatabaseSeeder.seedProduction(unitRepo)
    assertEquals(17, unitRepo.getAll().size)
}
```

## References

- Common mobile seeding patterns: [Android Room pre-population](https://developer.android.com/training/data-storage/room/prepopulate)
- KMP environment detection: [Kotlin expect/actual](https://kotlinlang.org/docs/multiplatform-connect-to-apis.html)
- Alternative: [Liquibase for mobile](https://www.liquibase.com/) (overkill for most apps)
