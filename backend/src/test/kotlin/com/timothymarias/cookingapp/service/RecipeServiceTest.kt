package com.timothymarias.cookingapp.service

import com.timothymarias.cookingapp.dto.RecipeCreationDto
import com.timothymarias.cookingapp.dto.RecipeDto
import com.timothymarias.cookingapp.dto.RecipeUpdateDto
import com.timothymarias.cookingapp.entity.Recipe
import com.timothymarias.cookingapp.mapper.RecipeMapper
import com.timothymarias.cookingapp.repository.RecipeRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Optional

class RecipeServiceTest {

    private lateinit var repository: RecipeRepository
    private lateinit var mapper: RecipeMapper
    private lateinit var service: RecipeService

    @BeforeEach
    fun setup() {
        repository = mock()
        mapper = mock()
        service = RecipeService(repository, mapper)
    }

    @Test
    fun `findAll returns mapped list`() {
        val r1 = Recipe(id = 1L, name = "A")
        val r2 = Recipe(id = 2L, name = "B")
        whenever(repository.findAll()).thenReturn(listOf(r1, r2))
        whenever(mapper.toDto(r1)).thenReturn(RecipeDto(1L, "A", emptyList()))
        whenever(mapper.toDto(r2)).thenReturn(RecipeDto(2L, "B", emptyList()))

        val result = service.findAll()

        assertEquals(listOf(1L, 2L), result.map { it.id })
    }

    @Test
    fun `getRecipe returns dto when found`() {
        val entity = Recipe(id = 5L, name = "Soup")
        whenever(repository.findById(5L)).thenReturn(Optional.of(entity))
        whenever(mapper.toDto(entity)).thenReturn(RecipeDto(5L, "Soup", emptyList()))

        val dto = service.getRecipe(5L)

        assertEquals(5L, dto.id)
        assertEquals("Soup", dto.name)
    }

    @Test
    fun `getRecipe throws when not found`() {
        whenever(repository.findById(99L)).thenReturn(Optional.empty())
        assertThrows(NoSuchElementException::class.java) { service.getRecipe(99L) }
    }

    @Test
    fun `create maps, saves and returns dto`() {
        val creation = RecipeCreationDto(name = "Tacos", ingredients = listOf("Tortilla"))
        val newEntity = Recipe(id = null, name = "Tacos")
        val saved = Recipe(id = 10L, name = "Tacos")
        val mappedDto = RecipeDto(10L, "Tacos", emptyList())

        whenever(mapper.toEntity(creation)).thenReturn(newEntity)
        whenever(repository.save(newEntity)).thenReturn(saved)
        whenever(mapper.toDto(saved)).thenReturn(mappedDto)

        val result = service.create(creation)

        assertEquals(10L, result.id)
        assertEquals("Tacos", result.name)
    }

    @Test
    fun `update modifies name if provided and returns dto`() {
        val existing = Recipe(id = 3L, name = "Old")
        whenever(repository.findById(3L)).thenReturn(Optional.of(existing))
        whenever(repository.save(existing)).thenReturn(existing)
        whenever(mapper.toDto(existing)).thenReturn(RecipeDto(3L, "New", emptyList()))

        val dto = service.update(3L, RecipeUpdateDto(name = "New", ingredients = emptyList()))

        assertEquals("New", dto.name)
        Mockito.verify(repository).save(existing)
    }
}
