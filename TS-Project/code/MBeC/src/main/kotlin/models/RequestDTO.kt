package models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class RequestDTO(val operation: String, val data: JsonObject)