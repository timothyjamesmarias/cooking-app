package com.timothymarias.cookingapp.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.*

@Entity
@Table(name = "ingredients")
class Ingredient(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String,

    // For sync engine: store the frontend's local_id
    @Column(name = "local_id", unique = true)
    var localId: String? = null,

    @OneToMany(mappedBy = "ingredient")
    @JsonIgnoreProperties("ingredient")
    var recipeIngredients: MutableSet<RecipeIngredient> = mutableSetOf(),

    // Helper property to maintain backward compatibility
    @Transient
    var recipes: MutableSet<Recipe> = mutableSetOf()
        get() = recipeIngredients.map { it.recipe }.toMutableSet()
) {
    constructor() : this(null, "", null, mutableSetOf())
}
