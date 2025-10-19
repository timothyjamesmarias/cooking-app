package com.timothymarias.cookingapp.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table

@Entity
@Table(name = "recipes")
class Recipe(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String,

    @ManyToMany
    @JoinTable(
        name = "recipe_ingredients",
        joinColumns = [JoinColumn(name = "recipe_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "ingredient_id", referencedColumnName = "id")]
    )
    var ingredients: MutableSet<Ingredient> = mutableSetOf()
) {
    constructor() : this(null, "", mutableSetOf())
}
