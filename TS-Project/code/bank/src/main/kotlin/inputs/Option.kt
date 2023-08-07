package inputs

enum class OPTION(val option: String, val validator: (String) -> Boolean) {
    BANK_PORT("-p", validatePort),
    AUTH_FILE("-s", validateFileName)
}