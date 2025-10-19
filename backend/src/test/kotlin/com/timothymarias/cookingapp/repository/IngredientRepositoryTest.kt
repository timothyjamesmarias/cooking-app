package com.timothymarias.cookingapp.repository

import com.timothymarias.cookingapp.entity.Ingredient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class IngredientRepositoryTest {

    @Autowired
    lateinit var ingredientRepository: IngredientRepository

    @Test
    fun `save and find by id`() {
        val saved = ingredientRepository.save(Ingredient(name = "Salt"))
        val found = ingredientRepository.findById(saved.id!!)
        assertThat(found).isPresent
        assertThat(found.get().name).isEqualTo("Salt")
    }

    @Test
    fun `findAll returns saved entities`() {
        ingredientRepository.save(Ingredient(name = "One"))
        ingredientRepository.save(Ingredient(name = "Two"))

        val all = ingredientRepository.findAll().toList()
        assertThat(all.map { it.name }).contains("One", "Two")
    }
}
