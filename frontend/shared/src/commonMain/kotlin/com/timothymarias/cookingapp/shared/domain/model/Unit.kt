package com.timothymarias.cookingapp.shared.domain.model

/**
 * Represents a unit of measurement for ingredients.
 *
 * @property localId Client-generated UUID for offline-first support
 * @property name Display name (e.g., "gram", "cup", "tablespoon")
 * @property symbol Short representation (e.g., "g", "c", "tbsp")
 * @property measurementType Category: WEIGHT, VOLUME, or COUNT
 * @property baseConversionFactor Multiplier to convert to base unit within type
 *                                (e.g., 1000 for kg→g, 0.25 for cup→liter)
 *                                Base units: gram (WEIGHT), milliliter (VOLUME), 1 (COUNT)
 *
 * Examples:
 * - Unit("uuid-1", "gram", "g", WEIGHT, 1.0)           // Base unit for weight
 * - Unit("uuid-2", "kilogram", "kg", WEIGHT, 1000.0)   // 1 kg = 1000 g
 * - Unit("uuid-3", "cup", "c", VOLUME, 236.588)        // 1 cup = 236.588 ml
 * - Unit("uuid-4", "whole", "", COUNT, 1.0)            // Base unit for counting
 */
data class Unit(
    val localId: String,
    val name: String,
    val symbol: String,
    val measurementType: MeasurementType,
    val baseConversionFactor: Double
)
