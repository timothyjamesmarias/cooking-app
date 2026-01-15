package com.timothymarias.cookingapp.repository

import com.timothymarias.cookingapp.entity.Quantity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface QuantityRepository : JpaRepository<Quantity, Long> {
    fun findByLocalId(localId: String): Quantity?
    fun existsByLocalId(localId: String): Boolean
    fun findAllByLocalIdIn(localIds: List<String>): List<Quantity>
    fun findByUnitId(unitId: Long): List<Quantity>
}