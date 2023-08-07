package models

import utils.parseStringToJsonObject

object Errors {
    object ProtocolError {
        const val CODE = 63
        const val MESSAGE = "protocol_error"
    }

    object LogicError {
        const val CODE = 125
        const val INVALID_ARGUMENTS = "Invalid arguments"
        const val INVALID_AUTH_FILE = "Auth file already exists"
        const val INVALID_OPERATION = "Invalid operation"
        const val LOGIC_OPERATION_NOT_ALLOWED = "Operation not allowed: logic failed"
        const val MAX_DELAY_TIME_EXPIRED = "The message took too much time to arrive, might be compromised"
    }

    fun errorResponse(message: String) = ResponseDTO(
        false,
        parseStringToJsonObject("{\"message\": \"$message\"}")
    )
}