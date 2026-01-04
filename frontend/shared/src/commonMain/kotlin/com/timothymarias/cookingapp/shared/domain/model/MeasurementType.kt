package com.timothymarias.cookingapp.shared.domain.model

/**
 * Categorizes units by measurement type for validation and conversion.
 *
 * Examples:
 * - WEIGHT: grams, ounces, pounds
 * - VOLUME: milliliters, cups, tablespoons
 * - COUNT: items, pieces, whole
 */
enum class MeasurementType {
    WEIGHT,
    VOLUME,
    COUNT
}
