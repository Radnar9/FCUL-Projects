package security

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

const val IV_SIZE = 16
const val KEY_SIZE = 32

fun encrypt(input: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC")
    val secretKeySpec = SecretKeySpec(key, "AES")
    val ivParameterSpec = IvParameterSpec(iv)
    cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
    return cipher.doFinal(input)
}

fun encryptWithIv(input: ByteArray, key: ByteArray, iv: ByteArray) = encrypt(input, key, iv).plus(iv)

fun decrypt(input: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC")
    val secretKeySpec = SecretKeySpec(key, "AES")
    val ivParameterSpec = IvParameterSpec(iv)
    cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
    return cipher.doFinal(input)
}

fun generateIv(): ByteArray {
    val iv = ByteArray(IV_SIZE)
    SecureRandom().nextBytes(iv)
    return iv
}

fun generateKey(): ByteArray {
    val random = SecureRandom()
    val keyBytes = ByteArray(KEY_SIZE)
    random.nextBytes(keyBytes)
    val key = SecretKeySpec(keyBytes, "AES")
    return key.encoded
}

fun retrieveEncryptedMsg(receivedMsg: ByteArray): ByteArray {
    return receivedMsg.slice(0 until receivedMsg.size - IV_SIZE).toByteArray()
}

fun retrieveIv(receivedMsg: ByteArray): ByteArray {
    return receivedMsg.slice(receivedMsg.size - IV_SIZE until receivedMsg.size).toByteArray()
}