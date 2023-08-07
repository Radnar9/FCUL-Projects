package security

import io.ktor.util.date.*

object SecurityMonitor {
    const val LIMIT_CHALLENGE_TIMEOUT: Long = 50
    private const val LIMIT_TIME: Long = 1000
    private const val MAX_COUNT = 10

    private var counter = 0
    private var timestamp: Long = 0
    var challengeActive = false

    fun updateMonitor() {
        val currentTimestamp = getTimeMillis()

        if(challengeActive && currentTimestamp - timestamp > LIMIT_TIME) {
            challengeActive = false
            timestamp = currentTimestamp
            counter = 1
        } else {
            counter += 1
            if (counter == 1) {
                timestamp = currentTimestamp
            }
            if (counter == MAX_COUNT) {
                challengeActive = currentTimestamp - timestamp < LIMIT_TIME
                counter = 0
                timestamp = currentTimestamp
            }
        }
    }
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