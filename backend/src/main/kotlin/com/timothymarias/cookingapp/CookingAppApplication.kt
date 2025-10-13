package com.timothymarias.cookingapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CookingAppApplication

fun main(args: Array<String>) {
    runApplication<CookingAppApplication>(*args)
}
