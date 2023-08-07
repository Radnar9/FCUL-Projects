package sockets

import authFileKey
import existingCards
import existingUsers
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.date.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.internal.synchronized
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.decodeFromString
import models.ResponseDTO
import models.Errors.ProtocolError
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonObject
import models.CommitInfo
import models.Errors.LogicError.MAX_DELAY_TIME_EXPIRED
import models.TimestampDTO
import security.*
import security.SecurityMonitor.LIMIT_CHALLENGE_TIMEOUT
import security.SecurityMonitor.updateMonitor
import utils.base64Decoder
import utils.base64Encoder
import utils.encodeObjToString
import java.io.InvalidObjectException
import java.time.Duration
import java.util.*


class SecureChannel(private val ip: String, private val port: Int) {
    companion object {
        private const val LIMIT_TIMEOUT: Long = 10  // Seconds
        private const val LIMIT_TIMESTAMP: Long = 1000 // Milliseconds
    }

    fun waitForMessage(onRequest: (input: String) -> CommitInfo, commit: (input: CommitInfo) -> Unit) {

        runBlocking {
            val selectorManager = SelectorManager(Dispatchers.IO)
            val serverSocket = aSocket(selectorManager).tcp().bind(ip, port)
            println("* Ready to receive connections *")
            while (true) {
                val socket = serverSocket.accept()

                launch {
                    val objToCommit = try {
                        val receiveChannel = socket.openReadChannel()
                        val sendChannel = socket.openWriteChannel(autoFlush = true)
                        // DH Protocol
                        val secret = dhProtocol(receiveChannel, sendChannel)

                        // Req/Res
                        val receivedMessage = withTimeout(LIMIT_TIMEOUT, receiveChannel) ?: throw Exception()

                        val input = base64Decoder(receivedMessage)
                        val decMessage = decrypt(retrieveEncryptedMsg(input), secret, retrieveIv(input)).toString(Charsets.UTF_8)
                        val objectMessage = Json.decodeFromString<TimestampDTO>(decMessage)
                        verifyTimestamp(objectMessage.timestamp)

                        val commitObj = onRequest(objectMessage.message)
                        val responseJSON = encodeObjToString(commitObj.responseDto)
                        val timeStampWrapper = TimestampDTO(getTimeMillis(), responseJSON)
                        val finalMessage = encodeObjToString(timeStampWrapper)
                        val encResponse = encryptWithIv(finalMessage.toByteArray(), secret, generateIv())
                        sendChannel.writeStringUtf8("${base64Encoder(encResponse)}\n")

                        commitObj
                    } catch (e: Exception) {
                        socket.close()
                        println(ProtocolError.MESSAGE)
                        null
                    }
                    if (objToCommit?.commitData != null) commit(objToCommit)
                }
            }
        }
    }

    private fun verifyTimestamp(timestamp: Long) {
        if(getTimeMillis() - timestamp > LIMIT_TIMESTAMP) throw Exception(MAX_DELAY_TIME_EXPIRED)
    }


    private suspend fun dhProtocol(receiveChannel: ByteReadChannel, sendChannel: ByteWriteChannel): ByteArray {
        val receivedMsg = withTimeout(LIMIT_TIMEOUT, receiveChannel) ?: throw Exception()

        challenge(receiveChannel, sendChannel)

        val encryptedMsg = base64Decoder(receivedMsg)
        val messageDecoded = decrypt(retrieveEncryptedMsg(encryptedMsg), authFileKey, retrieveIv(encryptedMsg))
        val objectMessage = Json.decodeFromString<TimestampDTO>(String(messageDecoded))

        verifyTimestamp(objectMessage.timestamp)

        val keyPair = generateKeyPair()
        val localPublicKey = getPublicKeyBytes(keyPair.public)

        val timeStampWrapper = TimestampDTO(getTimeMillis(), base64Encoder(localPublicKey))
        val finalMessage = encodeObjToString(timeStampWrapper)
        val encryptedRes = encryptWithIv(finalMessage.toByteArray(), authFileKey, generateIv())

        sendChannel.writeStringUtf8("${base64Encoder(encryptedRes)}\n")

        return doECDH(
            getPrivateKeyBytes(keyPair.private),
            base64Decoder(objectMessage.message)
        )
    }

    private suspend fun challenge(receiveChannel: ByteReadChannel, sendChannel: ByteWriteChannel) {
        updateMonitor()
        if (SecurityMonitor.challengeActive) {
            val challengeHash = createChallenge()
            sendChannel.writeStringUtf8("CHALLENGE:$challengeHash\n")

            val challengeResponse = withTimeout(LIMIT_CHALLENGE_TIMEOUT, receiveChannel) ?: throw Exception()

            if (!verifyChallenge(challengeHash, challengeResponse)) {
                throw Exception()
            }
        }
    }

    private suspend fun withTimeout(duration: Long, receiveChannel: ByteReadChannel): String? {
        return withTimeout(Duration.ofSeconds(duration).toMillis()) { receiveChannel.readUTF8Line() }
    }
}
