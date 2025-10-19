package com.timothymarias.cookingapp.mapper

import com.timothymarias.cookingapp.dto.RecipeCreationDto
import com.timothymarias.cookingapp.dto.RecipeDto
import com.timothymarias.cookingapp.entity.Recipe

class RecipeMapper {
    fun toDto(recipe: Recipe): RecipeDto {
        return RecipeDto(
            id = recipe.id,
            name = recipe.name,
            ingredients = recipe.ingredients.map { it.name }
        )
    }
    
    fun toEntity(recipeCreationDto: RecipeCreationDto): Recipe {
        val recipe = Recipe(
            id = null,
            name = recipeCreationDto.name
        )
        return recipe
    }
}