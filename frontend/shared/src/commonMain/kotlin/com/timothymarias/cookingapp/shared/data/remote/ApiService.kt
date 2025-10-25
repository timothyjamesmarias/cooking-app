package com.timothymarias.cookingapp.shared.data.remote

import com.timothymarias.cookingapp.shared.API_BASEURL
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
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
    }

    suspend inline fun <reified T> get(endpoint: String): T {
        return client.get("${API_BASEURL}$endpoint").body()
    }

    suspend inline fun <reified T> post(endpoint: String, body: Any): T {
        return client.post("${API_BASEURL}$endpoint") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }

    suspend inline fun <reified T> put(endpoint: String, body: Any): T {
        return client.put("${API_BASEURL}$endpoint") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
    }

    suspend fun delete(endpoint: String) {
        client.delete("${API_BASEURL}$endpoint")
    }

    fun close() {
        client.close()
    }
}