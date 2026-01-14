package com.timothymarias.cookingapp.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "recipes")
class Recipe(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String,

    // For sync engine: store the frontend's local_id
    @Column(name = "local_id", unique = true)
    var localId: String? = null,

    @Column(nullable = false)
    var version: Int = 1,

    @Column(name = "last_modified", nullable = false)
    var lastModified: Instant = Instant.now(),

    @OneToMany(mappedBy = "recipe", cascade = [CascadeType.ALL], orphanRemoval = true)
    var recipeIngredients: MutableSet<RecipeIngredient> = mutableSetOf()
) {
    constructor() : this(null, "", null, 1, Instant.now(), mutableSetOf())

    // Helper property to maintain backward compatibility
    val ingredients: MutableSet<Ingredient>
        get() = recipeIngredients.mapTo(mutableSetOf()) { it.ingredient }

    // Helper methods for managing ingredients
    fun addIngredient(ingredient: Ingredient, quantity: Quantity? = null) {
        val recipeIngredient = RecipeIngredient(this, ingredient, quantity)
        recipeIngredients.add(recipeIngredient)
    }

    fun removeIngredient(ingredient: Ingredient) {
        recipeIngredients.removeIf { it.ingredient.id == ingredient.id }
    }
}
