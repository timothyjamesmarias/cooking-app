package com.timothymarias.cookingapp.repository

import com.timothymarias.cookingapp.entity.RecipeIngredient
import com.timothymarias.cookingapp.entity.RecipeIngredientId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RecipeIngredientRepository : JpaRepository<RecipeIngredient, RecipeIngredientId> {
    fun findByRecipeId(recipeId: Long): List<RecipeIngredient>
    fun findByIngredientId(ingredientId: Long): List<RecipeIngredient>
    fun deleteByRecipeId(recipeId: Long)
}