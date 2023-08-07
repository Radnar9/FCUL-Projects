package models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ResponseDTO(val success: Boolean, val data: JsonObject)

object ResponseTimeout { const val LIMIT_TIMEOUT: Long = 100 }
