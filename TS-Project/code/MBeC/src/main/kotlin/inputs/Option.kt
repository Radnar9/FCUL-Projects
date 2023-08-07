package inputs

enum class OPTION(val option: String, val validator: (String) -> Boolean) {
    //Command line options
    ACCOUNT_NAME("-a", validateInteger),
    AUTH_FILE("-s", validateFileName),
    IP_BANK_OR_STORE_ADDRESS("-i", validateIPv4),
    BANK_OR_STORE_PORT("-p", validatePort),
    USER_FILE("-u", validateFileName),
    VIRTUAL_CREDIT_CARD_FILE("-v", validateFileName),

    //Modes of operation
    CREATE_ACCOUNT("-n", validateBalanceAmounts),
    DEPOSIT_AMOUNT("-d", validateBalanceAmounts),
    CREATE_VCC("-c", validateBalanceAmounts),
    GET_CURRENT_BALANCE("-g", validateEmptyValue),
    WITHDRAW_AMOUNT("-m", validateBalanceAmounts)
}