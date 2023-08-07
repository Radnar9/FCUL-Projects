package security

import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.interfaces.ECPrivateKey
import org.bouncycastle.jce.interfaces.ECPublicKey
import org.bouncycastle.jce.spec.ECParameterSpec
import org.bouncycastle.jce.spec.ECPrivateKeySpec
import org.bouncycastle.jce.spec.ECPublicKeySpec
import java.math.BigInteger
import java.security.*
import java.security.spec.ECGenParameterSpec
import javax.crypto.KeyAgreement


private val hexArray = "0123456789abcdef".toCharArray()

fun bytesToHex(bytes: ByteArray): String {
    val hexChars = CharArray(bytes.size * 2)
    for (j in bytes.indices) {
        val v = bytes[j].toInt() and 0xFF
        hexChars[j * 2] = hexArray[v ushr 4]
        hexChars[j * 2 + 1] = hexArray[v and 0x0F]
    }
    return String(hexChars)
}

fun getPublicKeyBytes(key: PublicKey): ByteArray {
    val eckey = key as ECPublicKey
    return eckey.q.getEncoded(true)
}

fun loadPublicKey(data: ByteArray?): PublicKey {
    val params: ECParameterSpec = ECNamedCurveTable.getParameterSpec("prime192v1")
    val pubKey = ECPublicKeySpec(
        params.curve.decodePoint(data), params
    )
    val kf = KeyFactory.getInstance("ECDH", "BC")
    return kf.generatePublic(pubKey)
}

fun getPrivateKeyBytes(key: PrivateKey): ByteArray {
    val eckey = key as ECPrivateKey
    return eckey.d.toByteArray()
}

fun loadPrivateKey(data: ByteArray?): PrivateKey {
    val params: ECParameterSpec = ECNamedCurveTable.getParameterSpec("prime192v1")
    val prvkey = ECPrivateKeySpec(BigInteger(data), params)
    val kf = KeyFactory.getInstance("ECDH", "BC")
    return kf.generatePrivate(prvkey)
}

fun generateKeyPair(): KeyPair {
    val kpGen = KeyPairGenerator.getInstance("ECDH", "BC")
    kpGen.initialize(ECGenParameterSpec("prime192v1"), SecureRandom())
    return kpGen.generateKeyPair()
}

fun doECDH(dataPrv: ByteArray?, dataPub: ByteArray?): ByteArray {
    val ka = KeyAgreement.getInstance("ECDH", "BC")
    ka.init(loadPrivateKey(dataPrv))
    ka.doPhase(loadPublicKey(dataPub), true)
    return ka.generateSecret()
}
