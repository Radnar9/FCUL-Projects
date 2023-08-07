package operations

import operations.handlers.*


class OperationInterpreter {
    private val operations = mapOf(
        Operations.CREATE_ACCOUNT to CreateAccountOperation(),
        Operations.CASH_DEPOSIT to CashDepositOperation(),
        Operations.GET_BALANCE to GetBalanceOperation(),
        Operations.CREATE_VCC to CreateVccOperation(),
        Operations.WITHDRAW_AMOUNT to WithdrawAmountOperation(),
    )

    fun getOperationHandler(operation: String): OperationHandler? = operations[operation]
}
