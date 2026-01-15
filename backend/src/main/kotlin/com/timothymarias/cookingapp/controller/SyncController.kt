package com.timothymarias.cookingapp.controller

import com.timothymarias.cookingapp.dto.sync.SyncRequestDto
import com.timothymarias.cookingapp.dto.sync.SyncResponseDto
import com.timothymarias.cookingapp.service.SyncService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.slf4j.LoggerFactory

@RestController
@RequestMapping("/api/sync")
@CrossOrigin(origins = ["*"]) // Configure appropriately for production
class SyncController(
    private val syncService: SyncService
) {
    private val logger = LoggerFactory.getLogger(SyncController::class.java)

    @PostMapping
    fun sync(@RequestBody request: SyncRequestDto): ResponseEntity<SyncResponseDto> {
        logger.info("Received sync request with ${request.entities.size} entities")

        return try {
            val response = syncService.processSync(request)

            val acceptedCount = response.results.count { it.accepted }
            val conflictCount = response.results.count { it.hasConflict }
            val errorCount = response.results.count { !it.accepted && !it.hasConflict }

            logger.info("Sync completed: $acceptedCount accepted, $conflictCount conflicts, $errorCount errors")

            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Error during sync: ${e.message}", e)
            ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf(
            "status" to "UP",
            "service" to "sync",
            "timestamp" to System.currentTimeMillis().toString()
        ))
    }
}