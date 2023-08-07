package sockets

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.date.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.Errors.LogicError.MAX_DELAY_TIME_EXPIRED
import models.ResponseDTO
import models.Errors.ProtocolError
import models.RequestDTO
import models.TimestampDTO
import security.*
import security.SecurityMonitor.LIMIT_TIMEOUT_MONITOR
import utils.printlnErr
import java.io.InvalidObjectException
import utils.*
import java.time.Duration
import kotlin.system.exitProcess

class SecureChannel(private val ip: String, private val port: Int, private val key: String) {
    companion object {
        private const val LIMIT_TIMEOUT: Long = 100
    }

    fun sendMessage(requestDTO: RequestDTO, onReturn: (input: ResponseDTO) -> Unit) {
        runBlocking {
            val selectorManager = SelectorManager(Dispatchers.IO)
            val socket = try {
                aSocket(selectorManager).tcp().connect(ip, port)
            } catch (e: Exception) {
                printlnErr("${ProtocolError.MESSAGE}: connection")
                exitProcess(ProtocolError.CODE)
            }

            val receiveChannel = socket.openReadChannel()
            val sendChannel = socket.openWriteChannel(autoFlush = true)

            launch(Dispatchers.IO) {
                try {
                    // DH Protocol
                    val secret = dhProtocol(receiveChannel, sendChannel, key)

                    // Req/Res
                    val requestJSON = encodeObjToString(requestDTO)
                    val timeStampWrapper = TimestampDTO(getTimeMillis(), requestJSON)
                    val finalMessage = encodeObjToString(timeStampWrapper)
                    val encRequest = encryptWithIv(finalMessage.toByteArray(), secret, generateIv())

                    sendChannel.writeStringUtf8("${base64Encoder(encRequest)}\n")

                    val receivedMessage = withTimeout(LIMIT_TIMEOUT, receiveChannel) ?: throw Exception()

                    val input = base64Decoder(receivedMessage)
                    val decMessage =
                        decrypt(retrieveEncryptedMsg(input), secret, retrieveIv(input)).toString(Charsets.UTF_8)
                    val objectMessage = Json.decodeFromString<TimestampDTO>(decMessage)
                    verifyTimestamp(objectMessage.timestamp)

                    onReturn(decodeStringToObj(objectMessage.message))
                } catch (e: Exception) {
                    closeSocketAndSelector(socket, selectorManager)
                    printlnErr("${ProtocolError.MESSAGE}: coroutine")
                    exitProcess(ProtocolError.CODE)
                } finally {
                    closeSocketAndSelector(socket, selectorManager)
                }
            }
        }
    }

    private fun verifyTimestamp(timestamp: Long) {
        if (getTimeMillis() - timestamp > 1000) throw Exception(MAX_DELAY_TIME_EXPIRED)
    }

    private suspend fun dhProtocol(
        receiveChannel: ByteReadChannel,
        sendChannel: ByteWriteChannel,
        key: String
    ): ByteArray {
        val keyPair = generateKeyPair()
        val localPublicKey = getPublicKeyBytes(keyPair.public)
        val authKey = base64Decoder(key)

        val timeStampWrapper = TimestampDTO(getTimeMillis(), base64Encoder(localPublicKey))
        val finalMessage = encodeObjToString(timeStampWrapper)
        val encryptedRes = encryptWithIv(finalMessage.toByteArray(), authKey, generateIv())

        sendChannel.writeStringUtf8("${base64Encoder(encryptedRes)}\n")

        val receivedMsg = withTimeout(LIMIT_TIMEOUT, receiveChannel) ?: throw Exception()

        challenge(receiveChannel, sendChannel)


        val encryptedMsg = base64Decoder(receivedMsg)
        val messageDecoded = decrypt(retrieveEncryptedMsg(encryptedMsg), authKey, retrieveIv(encryptedMsg))
        val objectMessage = Json.decodeFromString<TimestampDTO>(String(messageDecoded))

        verifyTimestamp(objectMessage.timestamp)

        return doECDH(
            getPrivateKeyBytes(keyPair.private),
            base64Decoder(objectMessage.message)
        )
    }

    private suspend fun challenge(receiveChannel: ByteReadChannel, sendChannel: ByteWriteChannel) {
        SecurityMonitor.updateMonitor()
        if (SecurityMonitor.challengeActive) {
            val challengeHash = createChallenge()
            sendChannel.writeStringUtf8("CHALLENGE:$challengeHash\n")

            val challengeResponse = withTimeout(LIMIT_TIMEOUT_MONITOR, receiveChannel) ?: throw Exception()

            if (!verifyChallenge(challengeHash, challengeResponse)) {
                throw Exception()
            }
            SecurityMonitor.deactivateAlarm()
        }
    }

    private suspend fun withTimeout(duration: Long, receiveChannel: ByteReadChannel): String? {
        return kotlinx.coroutines.withTimeout(Duration.ofSeconds(duration).toMillis()) { receiveChannel.readUTF8Line() }
    }

    private fun retrieveEncryptedMsg(receivedMsg: ByteArray): ByteArray {
        return receivedMsg.slice(0 until receivedMsg.size - IV_SIZE).toByteArray()
    }

    private fun retrieveIv(receivedMsg: ByteArray): ByteArray {
        return receivedMsg.slice(receivedMsg.size - IV_SIZE until receivedMsg.size).toByteArray()
    }

    private fun closeSocketAndSelector(socket: Socket, selectorManager: SelectorManager) {
        socket.close()
        selectorManager.close()
    }
}