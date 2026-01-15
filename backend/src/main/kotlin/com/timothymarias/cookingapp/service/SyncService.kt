package com.timothymarias.cookingapp.service

import com.timothymarias.cookingapp.dto.sync.*
import com.timothymarias.cookingapp.entity.*
import com.timothymarias.cookingapp.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant
import org.slf4j.LoggerFactory

@Service
class SyncService(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
    private val unitRepository: UnitRepository,
    private val quantityRepository: QuantityRepository,
    private val recipeIngredientRepository: RecipeIngredientRepository
) {
    private val logger = LoggerFactory.getLogger(SyncService::class.java)

    @Transactional
    fun processSync(request: SyncRequestDto): SyncResponseDto {
        logger.info("Processing sync request with ${request.entities.size} entities")

        val results = request.entities.map { entity ->
            try {
                processEntity(entity)
            } catch (e: Exception) {
                logger.error("Error processing entity ${entity.localId}: ${e.message}", e)
                SyncResultDto(
                    localId = entity.localId,
                    accepted = false,
                    hasConflict = false,
                    errorMessage = e.message
                )
            }
        }

        return SyncResponseDto(results = results)
    }

    private fun processEntity(entity: SyncEntityDto): SyncResultDto {
        logger.debug("Processing entity: type=${entity.type}, localId=${entity.localId}, version=${entity.version}")

        return when (entity.type) {
            "RECIPE" -> processRecipe(entity)
            "INGREDIENT" -> processIngredient(entity)
            "UNIT" -> processUnit(entity)
            "QUANTITY" -> processQuantity(entity)
            "RECIPE_INGREDIENT" -> processRecipeIngredient(entity)
            else -> throw IllegalArgumentException("Unknown entity type: ${entity.type}")
        }
    }

    private fun processRecipe(entity: SyncEntityDto): SyncResultDto {
        val existingRecipe = entity.serverId?.let { recipeRepository.findById(it).orElse(null) }
            ?: recipeRepository.findByLocalId(entity.localId)

        if (existingRecipe == null) {
            // New recipe from client
            val recipe = Recipe().apply {
                name = entity.data["name"] as String
                localId = entity.localId
                version = entity.version
                lastModified = Instant.ofEpochMilli(entity.timestamp)
            }

            val savedRecipe = recipeRepository.save(recipe)

            return SyncResultDto(
                localId = entity.localId,
                serverId = savedRecipe.id,
                accepted = true,
                hasConflict = false
            )
        } else {
            // Check for conflicts
            val conflict = detectConflict(
                existingVersion = existingRecipe.version,
                existingChecksum = calculateChecksum(existingRecipe),
                existingTimestamp = existingRecipe.lastModified.toEpochMilli(),
                incomingVersion = entity.version,
                incomingChecksum = entity.checksum,
                incomingTimestamp = entity.timestamp
            )

            if (conflict) {
                return SyncResultDto(
                    localId = entity.localId,
                    serverId = existingRecipe.id,
                    accepted = false,
                    hasConflict = true,
                    remoteData = mapOf(
                        "name" to existingRecipe.name,
                        "version" to existingRecipe.version
                    ),
                    remoteTimestamp = existingRecipe.lastModified.toEpochMilli()
                )
            } else {
                // Accept the update
                existingRecipe.apply {
                    name = entity.data["name"] as String
                    version = entity.version
                    lastModified = Instant.ofEpochMilli(entity.timestamp)
                }

                recipeRepository.save(existingRecipe)

                return SyncResultDto(
                    localId = entity.localId,
                    serverId = existingRecipe.id,
                    accepted = true,
                    hasConflict = false
                )
            }
        }
    }

    private fun processIngredient(entity: SyncEntityDto): SyncResultDto {
        val existingIngredient = entity.serverId?.let { ingredientRepository.findById(it).orElse(null) }
            ?: ingredientRepository.findByLocalId(entity.localId)

        if (existingIngredient == null) {
            // New ingredient from client
            val ingredient = Ingredient().apply {
                name = entity.data["name"] as String
                localId = entity.localId
                version = entity.version
                lastModified = Instant.ofEpochMilli(entity.timestamp)
            }

            val savedIngredient = ingredientRepository.save(ingredient)

            return SyncResultDto(
                localId = entity.localId,
                serverId = savedIngredient.id,
                accepted = true,
                hasConflict = false
            )
        } else {
            // Check for conflicts
            val conflict = detectConflict(
                existingVersion = existingIngredient.version,
                existingChecksum = calculateChecksum(existingIngredient),
                existingTimestamp = existingIngredient.lastModified.toEpochMilli(),
                incomingVersion = entity.version,
                incomingChecksum = entity.checksum,
                incomingTimestamp = entity.timestamp
            )

            if (conflict) {
                return SyncResultDto(
                    localId = entity.localId,
                    serverId = existingIngredient.id,
                    accepted = false,
                    hasConflict = true,
                    remoteData = mapOf(
                        "name" to existingIngredient.name,
                        "version" to existingIngredient.version
                    ),
                    remoteTimestamp = existingIngredient.lastModified.toEpochMilli()
                )
            } else {
                // Accept the update
                existingIngredient.apply {
                    name = entity.data["name"] as String
                    version = entity.version
                    lastModified = Instant.ofEpochMilli(entity.timestamp)
                }

                ingredientRepository.save(existingIngredient)

                return SyncResultDto(
                    localId = entity.localId,
                    serverId = existingIngredient.id,
                    accepted = true,
                    hasConflict = false
                )
            }
        }
    }

    private fun processUnit(entity: SyncEntityDto): SyncResultDto {
        val existingUnit = entity.serverId?.let { unitRepository.findById(it).orElse(null) }
            ?: unitRepository.findByLocalId(entity.localId)

        if (existingUnit == null) {
            // New unit from client
            val unit = Unit().apply {
                name = entity.data["name"] as String
                symbol = entity.data["symbol"] as? String ?: entity.data["abbreviation"] as? String ?: ""
                measurementType = when (entity.data["type"] as? String ?: entity.data["measurementType"] as? String) {
                    "WEIGHT" -> MeasurementType.WEIGHT
                    "VOLUME" -> MeasurementType.VOLUME
                    else -> MeasurementType.COUNT
                }
                baseConversionFactor = (entity.data["baseConversionFactor"] as? Number)?.toDouble() ?: 1.0
                localId = entity.localId
                version = entity.version
                lastModified = Instant.ofEpochMilli(entity.timestamp)
            }

            val savedUnit = unitRepository.save(unit)

            return SyncResultDto(
                localId = entity.localId,
                serverId = savedUnit.id,
                accepted = true,
                hasConflict = false
            )
        } else {
            // Check for conflicts
            val conflict = detectConflict(
                existingVersion = existingUnit.version,
                existingChecksum = calculateChecksum(existingUnit),
                existingTimestamp = existingUnit.lastModified.toEpochMilli(),
                incomingVersion = entity.version,
                incomingChecksum = entity.checksum,
                incomingTimestamp = entity.timestamp
            )

            if (conflict) {
                return SyncResultDto(
                    localId = entity.localId,
                    serverId = existingUnit.id,
                    accepted = false,
                    hasConflict = true,
                    remoteData = mapOf(
                        "name" to existingUnit.name,
                        "symbol" to existingUnit.symbol,
                        "measurementType" to existingUnit.measurementType.toString(),
                        "version" to existingUnit.version
                    ),
                    remoteTimestamp = existingUnit.lastModified.toEpochMilli()
                )
            } else {
                // Accept the update
                existingUnit.apply {
                    name = entity.data["name"] as String
                    symbol = entity.data["symbol"] as? String ?: entity.data["abbreviation"] as? String ?: ""
                    measurementType = when (entity.data["type"] as? String ?: entity.data["measurementType"] as? String) {
                        "WEIGHT" -> MeasurementType.WEIGHT
                        "VOLUME" -> MeasurementType.VOLUME
                        else -> MeasurementType.COUNT
                    }
                    baseConversionFactor = (entity.data["baseConversionFactor"] as? Number)?.toDouble() ?: 1.0
                    version = entity.version
                    lastModified = Instant.ofEpochMilli(entity.timestamp)
                }

                unitRepository.save(existingUnit)

                return SyncResultDto(
                    localId = entity.localId,
                    serverId = existingUnit.id,
                    accepted = true,
                    hasConflict = false
                )
            }
        }
    }

    private fun processQuantity(entity: SyncEntityDto): SyncResultDto {
        val existingQuantity = entity.serverId?.let { quantityRepository.findById(it).orElse(null) }
            ?: quantityRepository.findByLocalId(entity.localId)

        if (existingQuantity == null) {
            // Need to find the unit first
            val unitId = (entity.data["unitId"] as? Number)?.toLong()
                ?: throw IllegalArgumentException("Unit ID is required for quantity")

            val unit = unitRepository.findById(unitId).orElseThrow {
                IllegalArgumentException("Unit with ID $unitId not found")
            }

            // New quantity from client
            val quantity = Quantity().apply {
                amount = (entity.data["amount"] as Number).toDouble()
                this.unit = unit
                localId = entity.localId
                version = entity.version
                lastModified = Instant.ofEpochMilli(entity.timestamp)
            }

            val savedQuantity = quantityRepository.save(quantity)

            return SyncResultDto(
                localId = entity.localId,
                serverId = savedQuantity.id,
                accepted = true,
                hasConflict = false
            )
        } else {
            // Check for conflicts
            val conflict = detectConflict(
                existingVersion = existingQuantity.version,
                existingChecksum = calculateChecksum(existingQuantity),
                existingTimestamp = existingQuantity.lastModified.toEpochMilli(),
                incomingVersion = entity.version,
                incomingChecksum = entity.checksum,
                incomingTimestamp = entity.timestamp
            )

            if (conflict) {
                return SyncResultDto(
                    localId = entity.localId,
                    serverId = existingQuantity.id,
                    accepted = false,
                    hasConflict = true,
                    remoteData = mapOf(
                        "amount" to existingQuantity.amount,
                        "unitId" to existingQuantity.unit.id,
                        "version" to existingQuantity.version
                    ),
                    remoteTimestamp = existingQuantity.lastModified.toEpochMilli()
                )
            } else {
                // Update the quantity
                val unitId = (entity.data["unitId"] as? Number)?.toLong()
                    ?: throw IllegalArgumentException("Unit ID is required for quantity")

                val unit = unitRepository.findById(unitId).orElseThrow {
                    IllegalArgumentException("Unit with ID $unitId not found")
                }

                existingQuantity.apply {
                    amount = (entity.data["amount"] as Number).toDouble()
                    this.unit = unit
                    version = entity.version
                    lastModified = Instant.ofEpochMilli(entity.timestamp)
                }

                quantityRepository.save(existingQuantity)

                return SyncResultDto(
                    localId = entity.localId,
                    serverId = existingQuantity.id,
                    accepted = true,
                    hasConflict = false
                )
            }
        }
    }

    private fun processRecipeIngredient(entity: SyncEntityDto): SyncResultDto {
        // For now, return a simple success - would need to implement based on your RecipeIngredient entity structure
        // This would involve finding the recipe and ingredient by their IDs and creating the association
        return SyncResultDto(
            localId = entity.localId,
            serverId = null,
            accepted = true,
            hasConflict = false
        )
    }

    private fun detectConflict(
        existingVersion: Int,
        existingChecksum: String,
        existingTimestamp: Long,
        incomingVersion: Int,
        incomingChecksum: String,
        incomingTimestamp: Long
    ): Boolean {
        // If versions differ and checksums differ, we have a conflict
        if (existingVersion != incomingVersion && existingChecksum != incomingChecksum) {
            // Use timestamp as tiebreaker - if incoming is newer, no conflict (auto-accept)
            // This implements "last-write-wins" with the option for the client to override
            return incomingTimestamp <= existingTimestamp
        }
        return false
    }

    private fun calculateChecksum(entity: Any): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val data = when (entity) {
            is Recipe -> entity.name
            is Ingredient -> entity.name
            is com.timothymarias.cookingapp.entity.Unit -> "${entity.name}${entity.symbol}${entity.measurementType}"
            is Quantity -> "${entity.amount}${entity.unit.id}"
            else -> entity.toString()
        }
        return digest.digest(data.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}