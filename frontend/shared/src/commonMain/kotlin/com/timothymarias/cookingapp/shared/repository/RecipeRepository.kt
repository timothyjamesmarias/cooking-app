package com.timothymarias.cookingapp.shared.repository

import com.timothymarias.cookingapp.shared.api.ApiService
import com.timothymarias.cookingapp.shared.model.Recipe

class RecipeRepository {
    private val apiService = ApiService()
    private val BASE_ENDPOINT = "/recipes"

    suspend fun getAllRecipes(): List<Recipe> {
        return apiService.get(BASE_ENDPOINT)
    }

    suspend fun getRecipeById(id: Long): Recipe {
        return apiService.get("$BASE_ENDPOINT/$id")
    }

    suspend fun createRecipe(recipe: Recipe): Recipe {
        return apiService.post(BASE_ENDPOINT, recipe)
    }

    suspend fun updateRecipe(id: Long, recipe: Recipe): Recipe {
        return apiService.put("$BASE_ENDPOINT/$id", recipe)
    }

    suspend fun deleteRecipe(id: Long) {
        apiService.delete("$BASE_ENDPOINT/$id")
    }
}