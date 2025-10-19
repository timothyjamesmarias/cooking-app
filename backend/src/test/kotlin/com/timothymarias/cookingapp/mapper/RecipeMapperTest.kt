package com.timothymarias.cookingapp.mapper

import com.timothymarias.cookingapp.dto.RecipeCreationDto
import com.timothymarias.cookingapp.entity.Ingredient
import com.timothymarias.cookingapp.entity.Recipe
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RecipeMapperTest {

    private val mapper = RecipeMapper()

    @Test
    fun `toDto maps entity to dto including ingredient names`() {
        val sugar = Ingredient(id = 1L, name = "Sugar")
        val flour = Ingredient(id = 2L, name = "Flour")
        val recipe = Recipe(
            id = 42L,
            name = "Cake",
            ingredients = mutableSetOf(sugar, flour)
        )

        val dto = mapper.toDto(recipe)

        assertEquals(42L, dto.id)
        assertEquals("Cake", dto.name)
        assertEquals(listOf("Sugar", "Flour").sorted(), dto.ingredients.sorted())
    }

    @Test
    fun `toEntity maps creation dto to entity with null id and name set`() {
        val creation = RecipeCreationDto(
            name = "Bread",
            ingredients = listOf("Flour", "Water")
        )

        val entity = mapper.toEntity(creation)

        assertNull(entity.id)
        assertEquals("Bread", entity.name)
        // Current mapper ignores ingredients on creation; ensure empty set
        assertEquals(0, entity.ingredients.size)
    }
}
