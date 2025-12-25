package com.timothymarias.cookingapp.service

import com.timothymarias.cookingapp.dto.IngredientCreationDto
import com.timothymarias.cookingapp.dto.IngredientDto
import com.timothymarias.cookingapp.dto.IngredientUpdateDto
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

    fun update(ingredientDto: IngredientUpdateDto, id: Long): IngredientDto {
        val ingredientRecord = ingredientRepository.findById(id).orElseThrow()
        ingredientDto.name?.let { ingredientRecord.name = it }
        ingredientRepository.save(ingredientRecord)
        return ingredientMapper.toDto(ingredientRecord)
    }

    fun delete(id: Long) {
        val ingredientRecord = ingredientRepository.findById(id).orElseThrow()
        ingredientRepository.delete(ingredientRecord)
    }

    fun search(query: String): List<IngredientDto> {
        val results = ingredientRepository.searchIngredientsByName(query)
        return results.map { ingredientMapper.toDto(it) }
    }
}
