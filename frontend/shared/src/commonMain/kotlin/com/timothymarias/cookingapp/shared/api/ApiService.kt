package com.timothymarias.cookingapp.shared.api

import com.timothymarias.cookingapp.shared.API_BASEURL
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ApiService {
    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
            })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
    }

    suspend inline fun <reified T> get(endpoint: String): T {
        return client.get("$API_BASEURL$endpoint").body()
    }

    suspend inline fun <reified T> post(endpoint: String, body: Any): T {
        return client.post("$API_BASEURL$endpoint") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }

    suspend inline fun <reified T> put(endpoint: String, body: Any): T {
        return client.put("$API_BASEURL$endpoint") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }

    suspend fun delete(endpoint: String) {
        client.delete("$API_BASEURL$endpoint")
    }

    fun close() {
        client.close()
    }
}