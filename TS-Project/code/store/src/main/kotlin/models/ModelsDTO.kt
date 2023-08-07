package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class RequestDTO(val operation: String, val data: JsonObject)


@Serializable
data class ResponseDTO(val success: Boolean, val data: JsonObject)

@Serializable
data class TimestampDTO(
    @SerialName("timestamp") val timestamp: Long,
    @SerialName("message") val message: String,
)

@Serializable
data class WithdrawAmountResponseDTO(
    @SerialName("vcc_file") val vccFile: String,
    @SerialName("vcc_amount_used") val vccAmountUsed: Double
){
    override fun toString(): String
            = String.format("{\"vcc_file\":\"%s\",\"vcc_amount_used\":%.2f}",vccFile, vccAmountUsed)
}
