package models

object Errors {
    object ProtocolError {
        const val CODE = 63
        const val MESSAGE = "protocol_error"
    }

    object LogicError {
        const val CODE = 130
        const val INVALID_WITHDRAW_AMOUNT = "Invalid withdraw amount, must be greater than 0"
        const val INVALID_DEPOSIT_AMOUNT = "Invalid deposit amount, must be greater than 0"
        const val NON_EXISTENT_FILE = "The file doesn't exist or is empty"
        const val INVALID_ARGUMENTS = "Invalid arguments"
        const val INVALID_AUTH_FILE = "Auth file doesn't exist or is empty"
        const val INVALID_USER_FILE = "User file doesn't exist or is empty"
        const val INVALID_OPERATION = "Invalid operation"
        const val LOGIC_OPERATION_NOT_ALLOWED = "Operation not allowed: logic failed"
        const val MAX_DELAY_TIME_EXPIRED = "The message took too much time to arrive, might be compromised"
    }
}