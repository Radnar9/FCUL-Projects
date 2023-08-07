package sockets

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.Errors
import models.Errors.errorResponse
import models.RequestDTO
import models.ResponseDTO

class OutChannel(private val ip: String = "127.0.0.1", private val port: Int) {

    fun sendMessage(requestDto: RequestDTO, onReturn: (input: ResponseDTO) -> Unit) {
        runBlocking {
            val selectorManager = SelectorManager(Dispatchers.IO)
            val socket = aSocket(selectorManager).tcp().connect(ip, port)

            val receiveChannel = socket.openReadChannel()
            val sendChannel = socket.openWriteChannel(autoFlush = true)

            launch(Dispatchers.IO) {
                val response = receiveChannel.readUTF8Line()
                val responseDto =
                    if (response == null)
                        errorResponse(Errors.LogicError.INVALID_DATA)
                    else
                        Json.decodeFromString(response)
                onReturn(responseDto)
                println("\t<- Out res: $responseDto")

                socket.close()
                selectorManager.close()
                println("\tx- Out socket closed")
            }

            println("\t<- Out req: $requestDto")
            val requestJson = Json.encodeToString(requestDto)
            sendChannel.writeStringUtf8("${requestJson}\n")
        }
    }
}
