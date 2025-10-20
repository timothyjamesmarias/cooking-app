package com.timothymarias.cookingapp.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class Recipe(
    val id: Long? = null,
    val name: String,
    val ingredients: List<String>
)