package com.timothymarias.cookingapp.shared.sync.repository

import com.timothymarias.cookingapp.shared.data.repository.unit.UnitRepository
import com.timothymarias.cookingapp.shared.domain.model.MeasurementType
import com.timothymarias.cookingapp.shared.domain.model.Unit
import com.timothymarias.cookingapp.shared.sync.models.ChecksumGenerator
import com.timothymarias.cookingapp.shared.sync.models.EntityType
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Sync-aware wrapper for UnitRepository that tracks changes
 */
class SyncAwareUnitRepository(
    private val unitRepository: UnitRepository,
    private val syncRepository: SyncRepository
) {
    /**
     * Create a new unit with sync tracking
     */
    suspend fun createUnit(
        name: String,
        symbol: String,
        measurementType: MeasurementType,
        baseConversionFactor: Double = 1.0
    ): Unit {
        val localId = UUID.randomUUID().toString()
        val unit = Unit(
            localId = localId,
            name = name,
            symbol = symbol,
            measurementType = measurementType,
            baseConversionFactor = baseConversionFactor
        )

        // Create the unit
        val createdUnit = unitRepository.create(unit)

        // Initialize sync tracking
        val checksum = ChecksumGenerator.generateForUnit(
            localId,
            name,
            symbol,
            measurementType.name,
            baseConversionFactor
        )
        syncRepository.initializeSyncInfo(
            entityId = localId,
            entityType = EntityType.UNIT,
            checksum = checksum
        )

        return createdUnit
    }

    /**
     * Update a unit and mark it as dirty for sync
     */
    suspend fun updateUnit(unit: Unit): Unit {
        // Update the unit
        val updatedUnit = unitRepository.update(unit)

        // Mark as dirty for sync
        val checksum = ChecksumGenerator.generateForUnit(
            unit.localId,
            unit.name,
            unit.symbol,
            unit.measurementType.name,
            unit.baseConversionFactor
        )
        syncRepository.markDirty(
            entityId = unit.localId,
            checksum = checksum
        )

        return updatedUnit
    }

    /**
     * Delete a unit and clean up sync tracking
     */
    suspend fun deleteUnit(localId: String) {
        // Delete the unit
        unitRepository.delete(localId)

        // Clean up sync tracking
        syncRepository.deleteSyncInfo(localId)
        syncRepository.deleteConflictsForEntity(localId)
    }

    // ===== Pass-through methods (no sync tracking needed for reads) =====

    fun watchAll(): Flow<List<Unit>> = unitRepository.watchAll()
    fun watchById(localId: String): Flow<Unit?> = unitRepository.watchById(localId)
    fun watchByMeasurementType(type: MeasurementType): Flow<List<Unit>> =
        unitRepository.watchByMeasurementType(type)

    suspend fun getAll(): List<Unit> = unitRepository.getAll()
    suspend fun getById(localId: String): Unit? = unitRepository.getById(localId)
    suspend fun getByMeasurementType(type: MeasurementType): List<Unit> =
        unitRepository.getByMeasurementType(type)
    suspend fun searchByName(query: String): List<Unit> = unitRepository.searchByName(query)

    /**
     * Check if a unit has sync conflicts
     */
    suspend fun hasConflicts(unitId: String): Boolean {
        return syncRepository.hasConflicts(unitId)
    }

    /**
     * Get sync info for a unit
     */
    suspend fun getSyncInfo(unitId: String) = syncRepository.getSyncInfo(unitId)
}