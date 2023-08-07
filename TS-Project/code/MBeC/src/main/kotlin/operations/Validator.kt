package operations

import OPERATION
import inputs.OPTION

data class Operation(val name: OPERATION, val optionsRequired: List<OPTION>, val options: List<OPTION>)

private val possibleOptions = listOf(
    Operation(
        //-> Create account (-s, -i, -p, -u, -a, -n)
        OPERATION.CREATE_ACCOUNT,
        listOf(
            OPTION.ACCOUNT_NAME,
            OPTION.CREATE_ACCOUNT
        ),
        listOf(
            OPTION.AUTH_FILE,
            OPTION.IP_BANK_OR_STORE_ADDRESS,
            OPTION.BANK_OR_STORE_PORT,
            OPTION.USER_FILE,
            OPTION.ACCOUNT_NAME,
            OPTION.CREATE_ACCOUNT
        )
    ),
    Operation(
        //-> Deposit amount (-s, -i, -p, -u, -a, -d)
        OPERATION.CASH_DEPOSIT,
        listOf(
            OPTION.ACCOUNT_NAME,
            OPTION.DEPOSIT_AMOUNT
        ),
        listOf(
            OPTION.AUTH_FILE,
            OPTION.IP_BANK_OR_STORE_ADDRESS,
            OPTION.BANK_OR_STORE_PORT,
            OPTION.USER_FILE,
            OPTION.ACCOUNT_NAME,
            OPTION.DEPOSIT_AMOUNT
        )
    ),
    Operation(
        //-> Create virtual card (-s, -i, -p, -u, -a, -c)
        OPERATION.CREATE_VCC,
        listOf(
            OPTION.ACCOUNT_NAME,
            OPTION.CREATE_VCC
        ),
        listOf(
            OPTION.AUTH_FILE,
            OPTION.IP_BANK_OR_STORE_ADDRESS,
            OPTION.BANK_OR_STORE_PORT,
            OPTION.USER_FILE,
            OPTION.ACCOUNT_NAME,
            OPTION.CREATE_VCC
        )
    ),
    Operation(
        //-> Get current amount (-s, -i, -p, -u, -a, -g)
        OPERATION.GET_BALANCE,
        listOf(
            OPTION.ACCOUNT_NAME,
            OPTION.GET_CURRENT_BALANCE
        ),
        listOf(
            OPTION.AUTH_FILE,
            OPTION.IP_BANK_OR_STORE_ADDRESS,
            OPTION.BANK_OR_STORE_PORT,
            OPTION.USER_FILE,
            OPTION.ACCOUNT_NAME,
            OPTION.GET_CURRENT_BALANCE
        )
    ),
    Operation(
        //-> Withdraw the amount of money (-i, -p, -v, -m)
        OPERATION.WITHDRAW_AMOUNT,
        listOf(
            OPTION.WITHDRAW_AMOUNT
        ),
        listOf(
            OPTION.IP_BANK_OR_STORE_ADDRESS,
            OPTION.BANK_OR_STORE_PORT,
            OPTION.VIRTUAL_CREDIT_CARD_FILE,
            OPTION.WITHDRAW_AMOUNT
        )
    )
)

fun validateOperations(userOptions: List<OPTION>): OPERATION {

    for (possibleOption in possibleOptions) {

        if (userOptions.containsAll(possibleOption.optionsRequired)) {
            userOptions.forEach { option ->
                if (!possibleOption.options.contains(option))
                    return OPERATION.UNKNOWN
            }
            return possibleOption.name
        }
    }
    return OPERATION.UNKNOWN
}

