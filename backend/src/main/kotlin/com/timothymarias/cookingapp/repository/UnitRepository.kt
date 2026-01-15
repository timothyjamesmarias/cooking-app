package com.timothymarias.cookingapp.repository

import com.timothymarias.cookingapp.entity.Unit
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UnitRepository : JpaRepository<Unit, Long> {
    fun findByLocalId(localId: String): Unit?
    fun existsByLocalId(localId: String): Boolean
    fun findAllByLocalIdIn(localIds: List<String>): List<Unit>
    fun findByName(name: String): Unit?
    fun findByMeasurementType(measurementType: String): List<Unit>
}