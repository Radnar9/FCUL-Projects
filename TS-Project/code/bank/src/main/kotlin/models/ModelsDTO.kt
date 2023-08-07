package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserAccountInfo(
    val accountName: String,
    val pin: String,
    var balance: Double,
    val key: ByteArray,
    var vccSeqNumber: Int = 1,
)

@Serializable
data class UserFileInfoDTO(
    @SerialName("name") val accountName: String,
    @SerialName("account_pin") val accountPin: String,
    @SerialName("account_key") val accountKey: String,
)

@Serializable
data class AuthFileInfoDTO(
    @SerialName("key") val key: String,
    @SerialName("ip") val ip: String,
    @SerialName("port") val port: Int,
)

@Serializable
data class NewAccountDTO(
    @SerialName("account") val account: String,
    @SerialName("user_file") val userFile: String,
    @SerialName("initial_balance") val initialBalance: Double
)

@Serializable
data class NewDepositDTO(
    @SerialName("account") val account: String,
    @SerialName("user_file") val userFile: String,
    @SerialName("deposit_amount") val depositAmount: Double
)

@Serializable
data class DepositDTO(
    @SerialName("account") val account: String,
    @SerialName("deposit") val deposit: Double
) {
    override fun toString(): String
            = String.format("{\"account\":\"%s\",\"deposit\":%.2f}", account, deposit)
}

@Serializable
data class NewVccDTO(
    @SerialName("account") val account: String,
    @SerialName("user_file") val userFile: String,
    @SerialName("vcc_amount") val vccAmount: Double
)

@Serializable
data class NewVccCreatedDTO(
    @SerialName("user_file") val userFile: String,
    @SerialName("account") val account: String,
    @SerialName("vcc_number") val vccNumber: Int,
    @SerialName("vcc_amount") val vccAmount: Double
){
    override fun toString(): String
            = String.format("{\"vcc_file\":\"%s_%d.card\",\"vcc_amount_used\":%.2f}",account, vccNumber, vccAmount)
}

@Serializable
data class WithdrawAmountDTO(
    @SerialName("vcc_name") val vccName: String,
    @SerialName("encrypted_content") val encryptedContent: String
)

@Serializable
data class WithdrawAmountResponseDTO(
    @SerialName("vcc_file") val vccFile: String,
    @SerialName("vcc_amount_used") val vccAmountUsed: Double
)

@Serializable
data class WithdrawAmountEncryptedInfoDTO(
    @SerialName("vcc_content") val vccContent: NewVccCreatedDTO,
    @SerialName("purchase_amount") val purchaseAmount: Double,
    @SerialName("timestamp") val timestamp: Long,
)

@Serializable
data class GetBalanceDTO(
    @SerialName("account") val account: String,
    @SerialName("user_file") val userFile: String
)

@Serializable
data class BalanceDTO(
    @SerialName("account") val account: String,
    @SerialName("balance") val balance: Double
) {
    override fun toString(): String
            = String.format("{\"account\":\"%s\",\"balance\":%.2f}", account, balance)
}

@Serializable
data class VccDTO(
    @SerialName("account") val account: String,
    @SerialName("vcc_amount") var vccAmount: Double,
    @SerialName("vcc_file") val vccFile: String,
)  {
    override fun toString(): String
            = String.format("{\"account\":\"%s\",\"vcc_amount\":%.2f,\"vcc_file\":%s}", account, vccAmount, vccFile)
}

@Serializable
data class NewAccountPrint(
    @SerialName("account") val account: String,
    @SerialName("initial_balance") val initialBalance: Double
){
    override fun toString(): String
            = String.format("{\"account\":\"%s\",\"initial_balance\":%.2f}", account, initialBalance)
}


@Serializable
data class TimestampDTO(
    @SerialName("timestamp") val timestamp: Long,
    @SerialName("message") val message: String,
)
