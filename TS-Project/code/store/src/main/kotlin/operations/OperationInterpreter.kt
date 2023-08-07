package operations

import operations.Operations.Withdrawal

class OperationInterpreter {
    private val operations = mapOf<String, OperationHandler>(
        Withdrawal.WITHDRAW_AMOUNT to ShoppingOperation(),
    )

    fun getOperationHandler(operation: String): OperationHandler? = operations[operation]
}
