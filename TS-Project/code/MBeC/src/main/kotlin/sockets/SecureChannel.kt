package sockets

import fileManager.readFile
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.date.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import models.AuthFileInfoDTO
import models.Errors.LogicError.CODE
import models.Errors.LogicError.INVALID_AUTH_FILE
import models.Errors.LogicError.MAX_DELAY_TIME_EXPIRED
import models.Errors.ProtocolError
import models.RequestDTO
import models.ResponseDTO
import models.ResponseTimeout.LIMIT_TIMEOUT
import models.TimestampDTO
import security.*
import utils.*
import java.time.Duration
import kotlin.system.exitProcess

class SecureChannel(private val ip: String, private val port: Int, private val authFile: String) {

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
                    val secret = dhProtocol(receiveChannel, sendChannel)

                    // Req/Res
                    val requestJSON = encodeObjToString(requestDTO)
                    val timeStampWrapper = TimestampDTO(getTimeMillis(), requestJSON)
                    val finalMessage = encodeObjToString(timeStampWrapper)

                    val encRequest = encryptWithIv(finalMessage.toByteArray(), secret, generateIv())
                    sendChannel.writeStringUtf8("${base64Encoder(encRequest)}\n")

                    val receivedMessage = withTimeout(LIMIT_TIMEOUT, receiveChannel) ?: throw Exception()
                    val input = base64Decoder(receivedMessage)
                    val decMessage = decrypt(retrieveEncryptedMsg(input), secret, retrieveIv(input)).toString(Charsets.UTF_8)
                    val objectMessage = decodeStringToObj<TimestampDTO>(decMessage)
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

    private suspend fun dhProtocol(receiveChannel: ByteReadChannel, sendChannel: ByteWriteChannel): ByteArray {
        val keyPair = generateKeyPair()
        val localPublicKey = getPublicKeyBytes(keyPair.public)
        val authKey = getAuthKey(authFile)

        val timeStampWrapper = TimestampDTO(getTimeMillis(), base64Encoder(localPublicKey))
        val finalMessage = encodeObjToString(timeStampWrapper)
        val encryptedRes = encryptWithIv(finalMessage.toByteArray(), authKey, generateIv())

        sendChannel.writeStringUtf8("${base64Encoder(encryptedRes)}\n")

        var receivedMsg = withTimeout(LIMIT_TIMEOUT, receiveChannel) ?: throw Exception()

        receivedMsg = verifyChallenge(receivedMsg, receiveChannel, sendChannel)

        val encryptedMsg = base64Decoder(receivedMsg)
        val messageDecoded = decrypt(retrieveEncryptedMsg(encryptedMsg), authKey, retrieveIv(encryptedMsg))
        val objectMessage = decodeStringToObj<TimestampDTO>(String(messageDecoded))

        verifyTimestamp(objectMessage.timestamp)

        return doECDH(
            getPrivateKeyBytes(keyPair.private),
            base64Decoder(objectMessage.message)
        )
    }

    private fun verifyTimestamp(timestamp: Long) {
        if(getTimeMillis() - timestamp > 1000) throw Exception(MAX_DELAY_TIME_EXPIRED)
    }

    private suspend fun verifyChallenge(receivedMsg: String, receiveChannel: ByteReadChannel, sendChannel: ByteWriteChannel): String {
        if (receivedMsg.contains("CHALLENGE")) {
            val hash = receivedMsg.split(":")[1]
            println("Hash: $hash")
            println("Solving challenge...")
            val discoveredValue = discoveryValue(hash).toString()
            println("Discovered value: $discoveredValue")
            sendChannel.writeStringUtf8("$discoveredValue\n")
            return withTimeout(LIMIT_TIMEOUT, receiveChannel) ?: throw Exception()
        }
        return receivedMsg
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

    private fun getAuthKey(filename: String): ByteArray {
        return try {
            base64Decoder(
                decodeJsonObjectToObj<AuthFileInfoDTO>(
                    parseStringToJsonObject(readFile(filename))
                ).key
            )
        } catch (e: Exception) {
            printlnErr(INVALID_AUTH_FILE)
            exitProcess(CODE)
        }
    }


    private fun closeSocketAndSelector(socket: Socket, selectorManager: SelectorManager) {
        socket.dispose()
        selectorManager.close()
    }
}
