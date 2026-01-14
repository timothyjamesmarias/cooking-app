package com.timothymarias.cookingapp.repository

import com.timothymarias.cookingapp.entity.Recipe
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RecipeRepository : JpaRepository<Recipe, Long> {
    fun findByLocalId(localId: String): Recipe?
    fun existsByLocalId(localId: String): Boolean
    fun findAllByLocalIdIn(localIds: List<String>): List<Recipe>

    @Query("SELECT r FROM Recipe r LEFT JOIN FETCH r.recipeIngredients WHERE r.localId IN :localIds")
    fun findByLocalIdsWithIngredients(@Param("localIds") localIds: List<String>): List<Recipe>
}