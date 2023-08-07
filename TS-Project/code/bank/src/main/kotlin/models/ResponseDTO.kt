package models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ResponseDTO(val success: Boolean, val data: JsonObject)

data class CommitInfo(val operation: String, val commitData: JsonObject?, val responseDto: ResponseDTO)
