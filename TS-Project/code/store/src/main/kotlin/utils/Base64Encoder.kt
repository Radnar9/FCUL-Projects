package utils

import java.util.*

fun base64Decoder(str: String): ByteArray = Base64.getDecoder().decode(str)
fun base64Encoder(byteArray: ByteArray): String = Base64.getEncoder().encodeToString(byteArray)
