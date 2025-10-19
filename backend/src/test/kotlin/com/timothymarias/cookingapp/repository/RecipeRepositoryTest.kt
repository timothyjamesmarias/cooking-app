package com.timothymarias.cookingapp.repository

import com.timothymarias.cookingapp.entity.Recipe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class RecipeRepositoryTest {

    @Autowired
    lateinit var recipeRepository: RecipeRepository

    @Test
    fun `save and find by id`() {
        val saved = recipeRepository.save(Recipe(name = "Toast"))
        val found = recipeRepository.findById(saved.id!!)
        assertThat(found).isPresent
        assertThat(found.get().name).isEqualTo("Toast")
    }

    @Test
    fun `findAll returns saved entities`() {
        recipeRepository.save(Recipe(name = "One"))
        recipeRepository.save(Recipe(name = "Two"))

        val all = recipeRepository.findAll().toList()
        assertThat(all.map { it.name }).contains("One", "Two")
    }
}
