package operations

import authFile
import filemanager.readFile
import kotlinx.serialization.json.*
import models.*
import operations.Operations.Withdrawal.WITHDRAW_AMOUNT
import sockets.SecureChannel
import utils.decodeJsonObjectToObj
import utils.decodeStringToObj
import utils.encodeObjToJsonObject

class ShoppingOperation : OperationHandler {

    override fun processOperation(receivedData: JsonObject): ResponseDTO {
        val encryptedShoppingData = Json.decodeFromJsonElement<WithdrawAmountDTO>(receivedData)

        val readAuthFile = readFile(authFile)

        val authFile = decodeStringToObj<AuthFileInfoDTO>(readAuthFile)

        val request = RequestDTO(WITHDRAW_AMOUNT, encodeObjToJsonObject(encryptedShoppingData))
        var response = ResponseDTO(false, JsonObject(mapOf()))
        SecureChannel(authFile.ip, authFile.port, authFile.key).sendMessage(request) {
            response = it
        }
        if (response.success) println(decodeJsonObjectToObj<WithdrawAmountResponseDTO>(response.data))
        return response
    }
}