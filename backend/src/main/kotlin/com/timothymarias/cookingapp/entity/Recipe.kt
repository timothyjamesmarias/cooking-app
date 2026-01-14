package com.timothymarias.cookingapp.entity

import jakarta.persistence.*

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

    @OneToMany(mappedBy = "recipe", cascade = [CascadeType.ALL], orphanRemoval = true)
    var recipeIngredients: MutableSet<RecipeIngredient> = mutableSetOf(),

    // Helper property to maintain backward compatibility
    @Transient
    var ingredients: MutableSet<Ingredient> = mutableSetOf()
        get() = recipeIngredients.map { it.ingredient }.toMutableSet()
) {
    constructor() : this(null, "", null, mutableSetOf())

    // Helper methods for managing ingredients
    fun addIngredient(ingredient: Ingredient, quantity: Quantity? = null) {
        val recipeIngredient = RecipeIngredient(this, ingredient, quantity)
        recipeIngredients.add(recipeIngredient)
    }

    fun removeIngredient(ingredient: Ingredient) {
        recipeIngredients.removeIf { it.ingredient.id == ingredient.id }
    }
}
