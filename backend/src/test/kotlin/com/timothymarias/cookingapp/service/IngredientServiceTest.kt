package com.timothymarias.cookingapp.service

import com.timothymarias.cookingapp.dto.IngredientCreationDto
import com.timothymarias.cookingapp.dto.IngredientDto
import com.timothymarias.cookingapp.dto.IngredientUpdateDto
import com.timothymarias.cookingapp.entity.Ingredient
import com.timothymarias.cookingapp.mapper.IngredientMapper
import com.timothymarias.cookingapp.repository.IngredientRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Optional

class IngredientServiceTest {

    private lateinit var repository: IngredientRepository
    private lateinit var mapper: IngredientMapper
    private lateinit var service: IngredientService

    @BeforeEach
    fun setup() {
        repository = mock()
        mapper = mock()
        service = IngredientService(repository, mapper)
    }

    @Test
    fun `findAll maps to dto list`() {
        val i1 = Ingredient(id = 1L, name = "Salt")
        val i2 = Ingredient(id = 2L, name = "Sugar")
        whenever(repository.findAll()).thenReturn(listOf(i1, i2))
        whenever(mapper.toDto(i1)).thenReturn(IngredientDto(1L, "Salt", emptyList()))
        whenever(mapper.toDto(i2)).thenReturn(IngredientDto(2L, "Sugar", emptyList()))

        val result = service.findAll()

        assertEquals(listOf(1L, 2L), result.map { it.id })
    }

    @Test
    fun `create saves and returns dto`() {
        val creation = IngredientCreationDto(name = "Pepper", recipes = emptyList())
        val newEntity = Ingredient(id = null, name = "Pepper")
        val saved = Ingredient(id = 10L, name = "Pepper")
        val dto = IngredientDto(10L, "Pepper", emptyList())

        whenever(mapper.toEntity(creation)).thenReturn(newEntity)
        whenever(repository.save(newEntity)).thenReturn(saved)
        whenever(mapper.toDto(saved)).thenReturn(dto)

        val result = service.create(creation)

        assertEquals(10L, result.id)
        assertEquals("Pepper", result.name)
    }

    @Test
    fun `update modifies name and returns dto`() {
        val existing = Ingredient(id = 3L, name = "Old")
        val update = IngredientUpdateDto(name = "New", recipes = emptyList())
        whenever(repository.findById(3L)).thenReturn(Optional.of(existing))
        whenever(repository.save(existing)).thenReturn(existing)
        whenever(mapper.toDto(existing)).thenReturn(IngredientDto(3L, "New", emptyList()))

        val result = service.update(update, 3L)

        assertEquals("New", result.name)
        verify(repository).save(existing)
    }

    @Test
    fun `update throws when not found`() {
        whenever(repository.findById(99L)).thenReturn(Optional.empty())
        assertThrows(NoSuchElementException::class.java) {
            service.update(IngredientUpdateDto(name = "x", recipes = emptyList()), 99L)
        }
    }

    @Test
    fun `delete removes entity when found`() {
        val existing = Ingredient(id = 7L, name = "Oil")
        whenever(repository.findById(7L)).thenReturn(Optional.of(existing))

        service.delete(7L)

        verify(repository).delete(existing)
    }

    @Test
    fun `search maps repository results to dto`() {
        val i1 = Ingredient(id = 1L, name = "Salt")
        whenever(repository.searchIngredientsByName("sa")).thenReturn(listOf(i1))
        whenever(mapper.toDto(i1)).thenReturn(IngredientDto(1L, "Salt", emptyList()))

        val result = service.search("sa")

        assertEquals(listOf("Salt"), result.map { it.name })
    }
}
