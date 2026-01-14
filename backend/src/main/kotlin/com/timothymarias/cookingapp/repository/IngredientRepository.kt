package com.timothymarias.cookingapp.repository

import com.timothymarias.cookingapp.entity.Ingredient
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface IngredientRepository : JpaRepository<Ingredient, Long> {
    fun searchIngredientsByName(query: String): List<Ingredient>
    fun findByLocalId(localId: String): Ingredient?
    fun existsByLocalId(localId: String): Boolean
    fun findAllByLocalIdIn(localIds: List<String>): List<Ingredient>

    @Query("SELECT i FROM Ingredient i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun searchByNameContaining(@Param("query") query: String): List<Ingredient>
}