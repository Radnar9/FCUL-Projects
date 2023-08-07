package sockets

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import models.ResponseDTO
import models.Errors
import models.Errors.LogicError.INVALID_DATA
import models.Errors.errorResponse
import utils.encodeObjToString
import utils.printlnErr
import kotlin.system.exitProcess

class InChannel(private val ip: String, private val port: Int) {

    fun waitForMessage(onRequest: (input: String) -> ResponseDTO) {
        runBlocking {
            val selectorManager = SelectorManager(Dispatchers.IO)
            val serverSocket = aSocket(selectorManager).tcp().bind(ip, port)
            println("* Ready to receive connections *")
            while (true) {
                val socket = serverSocket.accept()
                launch {
                    val receiveChannel = socket.openReadChannel()
                    val sendChannel = socket.openWriteChannel(autoFlush = true)
                    try {
                        val input = receiveChannel.readUTF8Line()
                        val responseDto =
                            if (input == null)
                                errorResponse(INVALID_DATA)
                            else
                                onRequest(input)
                        val responseJson = encodeObjToString(responseDto)
                        sendChannel.writeStringUtf8("${responseJson}\n")
                    } catch (e: Throwable) {
                        socket.close()
                        printlnErr("${Errors.ProtocolError.MESSAGE}: coroutine")
                        exitProcess(Errors.ProtocolError.CODE)
                    } finally {
                        socket.close()
                    }
                }
            }
        }
    }
}
