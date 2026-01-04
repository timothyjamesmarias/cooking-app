package com.timothymarias.cookingapp.shared.domain.model

/**
 * Represents a measured amount with a specific unit.
 *
 * @property localId Client-generated UUID for offline-first support
 * @property amount Numeric value (e.g., 2.5, 150, 0.25)
 * @property unitId Foreign key reference to the Unit this quantity uses
 *
 * Examples:
 * - Quantity("qty-1", 250.0, "unit-gram-id")     // 250 grams
 * - Quantity("qty-2", 1.5, "unit-cup-id")        // 1.5 cups
 * - Quantity("qty-3", 3.0, "unit-whole-id")      // 3 whole items
 *
 * Note: The actual Unit object is resolved via repository/database join
 */
data class Quantity(
    val localId: String,
    val amount: Double,
    val unitId: String
)
