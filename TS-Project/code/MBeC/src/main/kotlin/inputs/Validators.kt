package inputs

fun validateArgs(args: Array<String>): List<OPTION> {

    val size = (args.sumOf { it.length } + args.size - 1)
    val options = mutableListOf<OPTION>()

    if(size > 4096) return emptyList()
    if(args.any{ s -> s.contains("--")}) return emptyList()

    for (i in args.indices) {
        val it = args[i]
        val a = if(it.length > 1) it.substring(0,2) else it
        if(a[0] == '-' && !OPTION.values().any{ o -> o.option == a}) {
            return emptyList()
        } else if(a[0] == '-') {
            val option = OPTION.values().find { o -> o.option == a }
            val valueToValidate =
                if(it.length > 2) { it.substring(2, it.length) }
                else if(i+1 in args.indices) args[i+1]
                else ""

            if(options.contains(option) || !option!!.validator(valueToValidate)) return emptyList()
            options.add(option)
        }
    }
    return options
}

val validateInteger: (String) -> Boolean = {
    if(it.toIntOrNull() != null) {
        if(it.toInt() < 0) false
        else !(it.length > 1 && it.first() == '0')
    } else false
}

val validateBalanceAmounts: (String) -> Boolean = {
    val regex = """^-?\d+\.\d{2}$""".toRegex()
    val double = it.toDoubleOrNull()
    ((double != null) && regex.matches(it) && (double >= 0.00) && (double <= 4294967295.99))
}

val validateFileName: (String) -> Boolean = {
    val fileName = it.substringBeforeLast('.')
    if (it == "." ||
        it == ".." ||
        fileName.isEmpty() ||
        fileName.length > 127) false
    else Regex("^[a-z0-9_.-]+$").matches(it)
}

val validateIPv4: (String) -> Boolean = {
    Regex("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$").matches(it)
}

val validatePort: (String) -> Boolean = {
    validateInteger(it) && 1024 <= it.toInt() && it.toInt() <= 65535
}

val validateEmptyValue: (String) -> Boolean = {
    it.isEmpty()
}