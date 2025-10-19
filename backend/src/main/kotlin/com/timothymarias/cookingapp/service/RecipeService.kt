package com.timothymarias.cookingapp.service

import com.timothymarias.cookingapp.dto.RecipeCreationDto
import com.timothymarias.cookingapp.dto.RecipeDto
import com.timothymarias.cookingapp.dto.RecipeUpdateDto
import com.timothymarias.cookingapp.mapper.RecipeMapper
import com.timothymarias.cookingapp.repository.RecipeRepository
import org.springframework.stereotype.Service

@Service
class RecipeService(
    private val recipeRepository: RecipeRepository,
    private val recipeMapper: RecipeMapper,
) {
    fun findAll(): List<RecipeDto> {
        return recipeRepository.findAll()
            .map { recipeMapper.toDto(it) }
    }

    fun getRecipe(id: Long): RecipeDto {
        val recipeRecord = recipeRepository.findById(id).orElseThrow()
        return recipeMapper.toDto(recipeRecord)
    }

    fun create(recipeCreationDto: RecipeCreationDto): RecipeDto {
        val recipe = recipeMapper.toEntity(recipeCreationDto)
        val savedRecipe = recipeRepository.save(recipe)
        return recipeMapper.toDto(savedRecipe)
    }

    fun update(id: Long, recipeDto: RecipeUpdateDto): RecipeDto {
        val recipeRecord = recipeRepository.findById(id).orElseThrow()
        recipeDto.name?.let { recipeRecord.name = it }
        recipeRepository.save(recipeRecord)
        return recipeMapper.toDto(recipeRecord)
    }
}