package com.timothymarias.cookingapp.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.*
import java.time.Instant

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

    @Column(nullable = false)
    var version: Int = 1,

    @Column(name = "last_modified", nullable = false)
    var lastModified: Instant = Instant.now(),

    @OneToMany(mappedBy = "ingredient")
    @JsonIgnoreProperties("ingredient")
    var recipeIngredients: MutableSet<RecipeIngredient> = mutableSetOf()
) {
    constructor() : this(null, "", null, 1, Instant.now(), mutableSetOf())

    // Helper property to maintain backward compatibility
    val recipes: MutableSet<Recipe>
        get() = recipeIngredients.mapTo(mutableSetOf()) { it.recipe }
}
