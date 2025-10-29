package com.timothymarias.cookingapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
//@ComponentScan(basePackages = ["com.timothymarias.cookingapp"])
class CookingAppApplication

fun main(args: Array<String>) {
    runApplication<CookingAppApplication>(*args)
}