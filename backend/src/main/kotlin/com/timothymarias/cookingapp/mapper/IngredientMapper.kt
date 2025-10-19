package com.timothymarias.cookingapp.mapper

import com.timothymarias.cookingapp.dto.IngredientCreationDto
import com.timothymarias.cookingapp.dto.IngredientDto
import com.timothymarias.cookingapp.entity.Ingredient
import org.springframework.stereotype.Component

@Component
class IngredientMapper {
    fun toDto(ingredient: Ingredient): IngredientDto {
        return IngredientDto(
            id = ingredient.id,
            name = ingredient.name,
            recipes = TODO()
        )
    }

    fun toEntity(ingredientCreationDto: IngredientCreationDto): Ingredient {
        val ingredient = Ingredient(
            id = null,
            name = ingredientCreationDto.name
        )
        return ingredient
    }
}