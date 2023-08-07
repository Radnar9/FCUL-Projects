package models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import utils.parseStringToJsonObject

object Errors {
    object ProtocolError {
        const val CODE = 63
        const val MESSAGE = "protocol_error"
    }

    object LogicError {
        const val CODE = 135
        const val INVALID_WITHDRAW_AMOUNT = "Invalid withdraw amount, must be greater than 0"
        const val NON_EXISTENT_FILE = "The auth file doesn't exist or is empty"
        const val INVALID_DATA = "Invalid data"
        const val INVALID_OPERATION = "Invalid operation"
        const val INVALID_ARGUMENTS = "Invalid arguments"
        const val MAX_DELAY_TIME_EXPIRED = "The message took too much time to arrive, might be compromised"


    }

    fun errorResponse(message: String) = ResponseDTO(
        false,
        parseStringToJsonObject("{\"message\": \"$message\"}")
    )
}


@Serializable
data class ErrorDTO(@SerialName("error") val message: String)