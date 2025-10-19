package com.timothymarias.cookingapp.controller

import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/recipe-drafts")
@Validated
class RecipeDraftsController() {}