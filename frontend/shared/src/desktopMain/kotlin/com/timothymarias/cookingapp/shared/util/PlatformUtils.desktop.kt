package com.timothymarias.cookingapp.shared.util

import java.util.UUID

/**
 * Desktop (JVM) implementation of platform utilities
 */

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun randomUUID(): String = UUID.randomUUID().toString()