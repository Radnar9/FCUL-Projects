package security

fun discoveryValue(targetHash: String): String? {
    for (i in 0 until Int.MAX_VALUE) {
        val value = "$i"
        if (value.hashCode().toString() == targetHash)
            return value
    }
    return null
}