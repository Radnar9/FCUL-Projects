package inputs

fun validateArgs(args: Array<String>): Boolean {

    val size = (args.sumOf { it.length } + args.size - 1)

    if(size > 4096) return false
    if(args.any{ s -> s.contains("--")}) return false

    for (i in args.indices) {
        val it = args[i]
        val a = if(it.length > 1) it.substring(0,2) else it
        if(a[0] == '-' && !OPTION.values().any{ o -> o.option == a}) {
            return false
        } else if(a[0] == '-') {
            val option = OPTION.values().find { o -> o.option == a }
            val valueToValidate =
                if(it.length > 2) { it.substring(2, it.length) }
                else if(i+1 in args.indices) args[i+1]
                else ""
            if(!option!!.validator(valueToValidate)) return false
        }
    }
    return true
}

val validateInteger: (String) -> Boolean = {
    if(it.toIntOrNull() != null) {
        if(it.toInt() < 0) false
        else !(it.length > 1 && it.first() == '0')
    } else false
}

val validateFileName: (String) -> Boolean = {
    val fileName = it.substringBeforeLast('.')
    if (it == "." ||
        it == ".." ||
        fileName.isEmpty() ||
        fileName.length > 127) false
    else Regex("^[a-z0-9_.-]+$").matches(it)
}

val validatePort: (String) -> Boolean = {
    validateInteger(it) && 1024 <= it.toInt() && it.toInt() <= 65535
}