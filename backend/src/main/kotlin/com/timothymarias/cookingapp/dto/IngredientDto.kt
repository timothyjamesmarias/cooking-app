package com.timothymarias.cookingapp.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class IngredientDto(
    val id: Long?,
    val name: String,
    val recipes: List<String>
)

data class IngredientCreationDto(
    @field:NotBlank(message = "Ingredient name is required")
    @field:Size(min = 1, max = 100, message = "ingredient name must be between 1 and 100 characters")
    val name: String,

    @field:Size(min = 0)
    val recipes: List<String>
)

data class IngredientUpdateDto(
    @field:NotBlank(message = "Ingredient name is required")
    @field:Size(min = 1, max = 100, message = "ingredient name must be between 1 and 100 characters")
    val name: String,

    @field:Size(min = 0)
    val recipes: List<String>
)
