package com.timothymarias.cookingapp.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.timothymarias.cookingapp.dto.RecipeCreationDto
import com.timothymarias.cookingapp.dto.RecipeDto
import com.timothymarias.cookingapp.dto.RecipeUpdateDto
import com.timothymarias.cookingapp.service.RecipeService
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(RecipeController::class)
@org.springframework.test.context.ActiveProfiles("test")
class RecipeControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockBean
    lateinit var recipeService: RecipeService

    @Test
    fun `GET all recipes returns list`() {
        val dtos = listOf(
            RecipeDto(1L, "Pancakes", listOf("Flour")),
            RecipeDto(2L, "Omelette", listOf("Eggs"))
        )
        given(recipeService.findAll()).willReturn(dtos)

        mockMvc.perform(get("/recipes") )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].name").value("Pancakes"))
            .andExpect(jsonPath("$[1].id").value(2))
    }

    @Test
    fun `GET recipe by id returns item`() {
        val dto = RecipeDto(5L, "Soup", listOf("Water"))
        given(recipeService.getRecipe(5L)).willReturn(dto)

        mockMvc.perform(get("/recipes/5"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(5))
            .andExpect(jsonPath("$.name").value("Soup"))
    }

    @Test
    fun `POST create recipe validates and returns created`() {
        val creation = RecipeCreationDto(name = "Salad", ingredients = listOf("Lettuce"))
        val created = RecipeDto(10L, "Salad", listOf("Lettuce"))
        given(recipeService.create(creation)).willReturn(created)

        mockMvc.perform(
            post("/recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creation))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(10))
            .andExpect(jsonPath("$.name").value("Salad"))
    }

    @Test
    fun `PUT update recipe returns updated`() {
        val update = RecipeUpdateDto(name = "New Name", ingredients = listOf("X"))
        val updated = RecipeDto(3L, "New Name", listOf("X"))
        given(recipeService.update(3L, update)).willReturn(updated)

        mockMvc.perform(
            put("/recipes/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("New Name"))
    }

    @Test
    fun `DELETE recipe returns 200`() {
        mockMvc.perform(
            delete("/recipes/3")
        ).andExpect(status().isOk)
    }
}
