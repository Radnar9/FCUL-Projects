package security

import io.ktor.util.date.*

object SecurityMonitor {
    const val LIMIT_TIMEOUT_MONITOR: Long = 50
    private const val LIMIT_TIME: Long = 1000
    private const val MAX_COUNT = 10

    private var counter = 0
    private var timestamp: Long = 0
    var challengeActive = false

    fun updateMonitor() {
        val timestamp = getTimeMillis()
        counter += 1

        if (counter == 1) {
            SecurityMonitor.timestamp = timestamp
        }
        if (counter == MAX_COUNT) {
            if (timestamp - SecurityMonitor.timestamp < LIMIT_TIME) {
                challengeActive = true
            }
            counter = 0
            this.timestamp = timestamp
        }
    }

    fun deactivateAlarm() { challengeActive = false }
}

fun createChallenge(): Int {
    val randomValue = (0 until Int.MAX_VALUE).random()
    println("Random: $randomValue")
    println("Hash: ${"$randomValue".hashCode()}")
    return "$randomValue".hashCode()
}

fun verifyChallenge(hashCode: Int, value: String?): Boolean {
    return hashCode == value.hashCode()
}