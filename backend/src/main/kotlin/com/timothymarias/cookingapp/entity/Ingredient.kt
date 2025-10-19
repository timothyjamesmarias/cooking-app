package com.timothymarias.cookingapp.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table

@Entity
@Table(name = "ingredients")
class Ingredient(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String,

    @ManyToMany(mappedBy = "ingredients")
    @JsonIgnoreProperties("ingredients")
    var recipes: MutableSet<Recipe> = mutableSetOf()
) {
    constructor() : this(null, "", mutableSetOf())
}
