package com.timothymarias.cookingapp.repository

import com.timothymarias.cookingapp.entity.Recipe
import org.springframework.data.repository.CrudRepository

interface RecipeRepository: CrudRepository<Recipe, Long>