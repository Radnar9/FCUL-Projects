package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WithdrawAmountDTO(
    @SerialName("vcc_name") val vccName: String,
    @SerialName("encrypted_content") val encryptedContent: String
)

@Serializable
data class AuthFileInfoDTO(
    @SerialName("key") val key: String,
    @SerialName("ip") val ip: String,
    @SerialName("port") val port: Int,
)
