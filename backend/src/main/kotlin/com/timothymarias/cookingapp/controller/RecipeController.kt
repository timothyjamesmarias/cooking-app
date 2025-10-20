package com.timothymarias.cookingapp.controller

import com.timothymarias.cookingapp.common.ApiConstants
import com.timothymarias.cookingapp.dto.RecipeCreationDto
import com.timothymarias.cookingapp.dto.RecipeDto
import com.timothymarias.cookingapp.dto.RecipeUpdateDto
import com.timothymarias.cookingapp.service.RecipeService
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("${ApiConstants.API_V1_BASE_PATH}/recipes")
@Validated
class RecipeController(
    private val recipeService: RecipeService
) {

    @GetMapping("")
    fun getAllRecipes() : List<RecipeDto> {
        return recipeService.findAll()
    }

    @GetMapping("/{id}")
    fun getRecipe(@PathVariable id: Long): RecipeDto {
        return recipeService.getRecipe(id)
    }

    @PostMapping("")
    fun createRecipe(@Valid @RequestBody recipeDto: RecipeCreationDto): RecipeDto {
        return recipeService.create(recipeDto)
    }

    @PutMapping("/{id}")
    fun updateRecipe(
        @PathVariable id: Long,
        @Valid @RequestBody recipeDto: RecipeUpdateDto
    ): RecipeDto {
        return recipeService.update(id, recipeDto)
    }

    @DeleteMapping("/{id}")
    fun deleteRecipe(@PathVariable id: Long) {
        return recipeService.delete(id)
    }
}