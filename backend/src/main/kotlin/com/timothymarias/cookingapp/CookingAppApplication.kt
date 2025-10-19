package com.timothymarias.cookingapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class CookingAppApplication

fun main(args: Array<String>) {
    runApplication<CookingAppApplication>(*args)
}

@RestController
@RequestMapping("/api/v1")
class BaseControllerV1