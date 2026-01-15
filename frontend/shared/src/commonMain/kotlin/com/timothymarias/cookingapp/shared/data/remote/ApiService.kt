package com.timothymarias.cookingapp.shared.data.remote

import com.timothymarias.cookingapp.shared.API_BASEURL
import com.timothymarias.cookingapp.shared.data.remote.dto.SyncRequestDto
import com.timothymarias.cookingapp.shared.data.remote.dto.SyncResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
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
        install(HttpTimeout) {
            requestTimeoutMillis = 30000 // 30 seconds for sync operations
            connectTimeoutMillis = 10000 // 10 seconds to connect
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

    // Sync-specific methods

    /**
     * Performs a sync operation with the backend
     * @return SyncResponseDto with results for each entity
     */
    suspend fun sync(request: SyncRequestDto): Result<SyncResponseDto> {
        return try {
            val response = post<SyncResponseDto>("/api/sync", request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Checks if the sync service is available
     * @return true if the service is reachable, false otherwise
     */
    suspend fun checkSyncHealth(): Boolean {
        return try {
            val response: HttpResponse = client.get("${API_BASEURL}/api/sync/health")
            response.status.isSuccess()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks general connectivity to the backend
     * @return true if backend is reachable, false otherwise
     */
    suspend fun isBackendAvailable(): Boolean {
        return try {
            val response: HttpResponse = client.get("${API_BASEURL}/actuator/health")
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            false
        }
    }

    fun close() {
        client.close()
    }
}