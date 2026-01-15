package com.timothymarias.cookingapp.entity

import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(name = "recipe_ingredients")
@IdClass(RecipeIngredientId::class)
class RecipeIngredient(
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    var recipe: Recipe,

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id")
    var ingredient: Ingredient,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quantity_id")
    var quantity: Quantity? = null
) {
    constructor() : this(Recipe(), Ingredient(), null)
}

// Composite key class for RecipeIngredient
class RecipeIngredientId(
    var recipe: Long? = null,
    var ingredient: Long? = null
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RecipeIngredientId) return false
        return recipe == other.recipe && ingredient == other.ingredient
    }

    override fun hashCode(): Int {
        return 31 * (recipe?.hashCode() ?: 0) + (ingredient?.hashCode() ?: 0)
    }
}