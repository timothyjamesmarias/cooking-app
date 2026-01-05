package com.timothymarias.cookingapp.shared.data.local

import com.timothymarias.cookingapp.shared.data.repository.unit.UnitRepository
import com.timothymarias.cookingapp.shared.domain.model.MeasurementType
import com.timothymarias.cookingapp.shared.domain.model.Unit
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

/**
 * Seeds the database with initial reference data.
 * Designed to be run once at app initialization.
 */
object DatabaseSeeder {
    /**
     * Seeds common units if the units table is empty.
     * Returns true if seeding occurred, false if units already existed.
     */
    suspend fun seedUnitsIfEmpty(unitRepository: UnitRepository): Boolean {
        val existing = unitRepository.getAll()
        if (existing.isNotEmpty()) {
            return false // Already seeded
        }

        // Seed common units
        commonUnits.forEach { unit ->
            unitRepository.create(unit)
        }

        return true // Seeding completed
    }

    /**
     * Common units pre-populated in the app.
     * Base units: gram (WEIGHT), milliliter (VOLUME), whole (COUNT)
     *
     * Conversion factors convert TO base unit:
     * - 1 kilogram = 1000 grams → factor = 1000
     * - 1 cup = 236.588 milliliters → factor = 236.588
     */
    private val commonUnits = listOf(
        // WEIGHT units (base: gram)
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "gram",
            symbol = "g",
            measurementType = MeasurementType.WEIGHT,
            baseConversionFactor = 1.0
        ),
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "kilogram",
            symbol = "kg",
            measurementType = MeasurementType.WEIGHT,
            baseConversionFactor = 1000.0
        ),
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "ounce",
            symbol = "oz",
            measurementType = MeasurementType.WEIGHT,
            baseConversionFactor = 28.3495
        ),
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "pound",
            symbol = "lb",
            measurementType = MeasurementType.WEIGHT,
            baseConversionFactor = 453.592
        ),

        // VOLUME units (base: milliliter)
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "milliliter",
            symbol = "ml",
            measurementType = MeasurementType.VOLUME,
            baseConversionFactor = 1.0
        ),
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "liter",
            symbol = "L",
            measurementType = MeasurementType.VOLUME,
            baseConversionFactor = 1000.0
        ),
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "teaspoon",
            symbol = "tsp",
            measurementType = MeasurementType.VOLUME,
            baseConversionFactor = 4.92892
        ),
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "tablespoon",
            symbol = "tbsp",
            measurementType = MeasurementType.VOLUME,
            baseConversionFactor = 14.7868
        ),
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "cup",
            symbol = "c",
            measurementType = MeasurementType.VOLUME,
            baseConversionFactor = 236.588
        ),
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "pint",
            symbol = "pt",
            measurementType = MeasurementType.VOLUME,
            baseConversionFactor = 473.176
        ),
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "quart",
            symbol = "qt",
            measurementType = MeasurementType.VOLUME,
            baseConversionFactor = 946.353
        ),
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "gallon",
            symbol = "gal",
            measurementType = MeasurementType.VOLUME,
            baseConversionFactor = 3785.41
        ),
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "fluid ounce",
            symbol = "fl oz",
            measurementType = MeasurementType.VOLUME,
            baseConversionFactor = 29.5735
        ),

        // COUNT units (base: whole/piece)
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "whole",
            symbol = "",
            measurementType = MeasurementType.COUNT,
            baseConversionFactor = 1.0
        ),
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "piece",
            symbol = "pc",
            measurementType = MeasurementType.COUNT,
            baseConversionFactor = 1.0
        ),
        Unit(
            localId = "unit-${UUID.generateUUID()}",
            name = "dozen",
            symbol = "dz",
            measurementType = MeasurementType.COUNT,
            baseConversionFactor = 12.0
        ),
    )
}
