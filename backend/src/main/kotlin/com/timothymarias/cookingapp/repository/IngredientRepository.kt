package com.timothymarias.cookingapp.repository

import com.timothymarias.cookingapp.entity.Ingredient
import org.springframework.data.repository.CrudRepository

interface IngredientRepository: CrudRepository<Ingredient, Long> {
    fun searchIngredientsByName(query: String): List<Ingredient>
}