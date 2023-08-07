package sockets

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import models.Errors.ProtocolError
import models.RequestDTO
import models.ResponseDTO
import models.ResponseTimeout.LIMIT_TIMEOUT
import security.discoveryValue
import utils.decodeStringToObj
import utils.encodeObjToString
import utils.printlnErr
import java.time.Duration
import kotlin.system.exitProcess

class OutChannel(private val ip: String, private val port: Int) {

    fun sendMessage(requestDto: RequestDTO, onReturn: (input: ResponseDTO) -> Unit) {
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
                    var response = withTimeout(LIMIT_TIMEOUT, receiveChannel) ?: throw Exception()
                    response = verifyChallenge(response, receiveChannel, sendChannel)
                    val responseDto = decodeStringToObj<ResponseDTO>(response)
                    onReturn(responseDto)
                } catch (e: Exception) {
                    closeSocketAndSelector(socket, selectorManager)
                    printlnErr("${ProtocolError.MESSAGE}: coroutine")
                    exitProcess(ProtocolError.CODE)
                } finally {
                    closeSocketAndSelector(socket, selectorManager)
                }
            }

            try {
                val requestJson = encodeObjToString(requestDto)
                sendChannel.writeStringUtf8("${requestJson}\n")
            } catch (e: Exception) {
                closeSocketAndSelector(socket, selectorManager)
                printlnErr("${ProtocolError.MESSAGE}: sending")
                exitProcess(ProtocolError.CODE)
            }
        }
    }

    private fun closeSocketAndSelector(socket: Socket, selectorManager: SelectorManager) {
        socket.close()
        selectorManager.close()
    }

    private suspend fun withTimeout(duration: Long, receiveChannel: ByteReadChannel): String? {
        return kotlinx.coroutines.withTimeout(Duration.ofSeconds(duration).toMillis()) { receiveChannel.readUTF8Line() }
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
}
