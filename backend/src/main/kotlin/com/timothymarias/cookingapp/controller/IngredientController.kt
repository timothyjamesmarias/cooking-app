package com.timothymarias.cookingapp.controller

import com.timothymarias.cookingapp.BaseControllerV1
import com.timothymarias.cookingapp.dto.IngredientCreationDto
import com.timothymarias.cookingapp.dto.IngredientDto
import com.timothymarias.cookingapp.service.IngredientService
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/ingredients")
@Validated
class IngredientController(
    private val ingredientService: IngredientService
): BaseControllerV1() {

   @RequestMapping("/search")
   fun search() : List<IngredientDto> {
       return ingredientService.findAll()
   }

    @PostMapping("")
    fun createIngredient(@Valid @RequestBody ingredientDto: IngredientCreationDto): IngredientDto {
        return ingredientService.create(ingredientDto)
    }
}