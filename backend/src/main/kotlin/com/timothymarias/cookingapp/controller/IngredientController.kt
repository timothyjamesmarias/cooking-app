package com.timothymarias.cookingapp.controller

import com.timothymarias.cookingapp.common.ApiConstants
import com.timothymarias.cookingapp.dto.IngredientCreationDto
import com.timothymarias.cookingapp.dto.IngredientDto
import com.timothymarias.cookingapp.dto.IngredientUpdateDto
import com.timothymarias.cookingapp.service.IngredientService
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("${ApiConstants.API_V1_BASE_PATH}/ingredients")
@Validated
class IngredientController(
    private val ingredientService: IngredientService
) {

   @PostMapping("/search")
   fun search(@Valid @RequestBody query: String) : List<IngredientDto> {
       return ingredientService.search(query)
   }

    @PostMapping("")
    fun createIngredient(@Valid @RequestBody ingredientDto: IngredientCreationDto): IngredientDto {
        return ingredientService.create(ingredientDto)
    }

    @PutMapping("/{id}")
    fun updateIngredient(
        @PathVariable id: Long,
        @Valid @RequestBody ingredientDto: IngredientUpdateDto
    ): IngredientDto {
        return ingredientService.update(ingredientDto, id)
    }

    @DeleteMapping("/{id}")
    fun deleteIngredient(@PathVariable id: Long) {
        return ingredientService.delete(id)
    }
}
