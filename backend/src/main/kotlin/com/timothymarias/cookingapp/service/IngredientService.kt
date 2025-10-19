package com.timothymarias.cookingapp.service

import com.timothymarias.cookingapp.dto.IngredientCreationDto
import com.timothymarias.cookingapp.dto.IngredientDto
import com.timothymarias.cookingapp.mapper.IngredientMapper
import com.timothymarias.cookingapp.repository.IngredientRepository
import org.springframework.stereotype.Service

@Service
class IngredientService(
    private val ingredientRepository: IngredientRepository,
    private val ingredientMapper: IngredientMapper,
) {
    fun findAll(): List<IngredientDto> {
        return ingredientRepository.findAll()
            .map { ingredientMapper.toDto(it) }
    }

    fun create(ingredientDto: IngredientCreationDto): IngredientDto {
        val ingredient = ingredientMapper.toEntity(ingredientDto)
        val savedIngredient = ingredientRepository.save(ingredient)
        return ingredientMapper.toDto(savedIngredient)
    }
}