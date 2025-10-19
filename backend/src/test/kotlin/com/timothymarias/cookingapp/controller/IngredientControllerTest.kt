package com.timothymarias.cookingapp.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.timothymarias.cookingapp.dto.IngredientCreationDto
import com.timothymarias.cookingapp.dto.IngredientDto
import com.timothymarias.cookingapp.dto.IngredientUpdateDto
import com.timothymarias.cookingapp.service.IngredientService
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(IngredientController::class)
@org.springframework.test.context.ActiveProfiles("test")
class IngredientControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockBean
    lateinit var ingredientService: IngredientService

    @Test
    fun `POST create ingredient returns created dto`() {
        val creation = IngredientCreationDto(name = "Salt", recipes = emptyList())
        val created = IngredientDto(1L, "Salt", emptyList())
        given(ingredientService.create(creation)).willReturn(created)

        mockMvc.perform(
            post("/ingredients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creation))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Salt"))
    }

    @Test
    fun `PUT update ingredient returns updated dto`() {
        val update = IngredientUpdateDto(name = "New", recipes = emptyList())
        val updated = IngredientDto(5L, "New", emptyList())
        given(ingredientService.update(update, 5L)).willReturn(updated)

        mockMvc.perform(
            put("/ingredients/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("New"))
    }

    @Test
    fun `DELETE ingredient returns 200`() {
        mockMvc.perform(
            delete("/ingredients/9")
        ).andExpect(status().isOk)
    }

    @Test
    fun `POST search returns list of ingredients`() {
        val results = listOf(
            IngredientDto(2L, "Sugar", emptyList())
        )
        given(ingredientService.search("su")).willReturn(results)

        mockMvc.perform(
            post("/ingredients/search")
                .contentType(MediaType.TEXT_PLAIN)
                .content("su")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].name").value("Sugar"))
    }
}
