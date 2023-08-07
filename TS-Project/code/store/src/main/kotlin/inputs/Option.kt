package inputs

enum class OPTION(val option: String, val validator: (String) -> Boolean) {
    STORE_PORT("-p", validatePort),
    AUTH_FILE("-s", validateFileName)
}