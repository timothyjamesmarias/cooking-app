package com.timothymarias.cookingapp.dto

data class RecipeDto(
    val id: Long?,
    val name: String,
    val ingredients: List<String>
)

data class RecipeCreationDto(
    @field:NotBlank(message = "Recipe name is required")
    @field:Size(min = 3, max = 100, message = "Recipe name must be between 3 and 100 characters")
    val name: String,

    @field:NotEmpty(message = "At least one ingredient is required")
    @field:Size(max = 20, message = "Maximum 20 ingredients allowed")
    val ingredients: List<String>
)

data class RecipeUpdateDto(
    @field:NotBlank(message = "Recipe name is required")
    @field:Size(min = 3, max = 100, message = "Recipe name must be between 3 and 100 characters")
    val name: String,

    @field:NotEmpty(message = "At least one ingredient is required")
    @field:Size(max = 20, message = "Maximum 20 ingredients allowed")
    val ingredients: List<String>
)
