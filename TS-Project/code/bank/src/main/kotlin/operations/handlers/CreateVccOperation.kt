package operations.handlers

import existingCards
import existingUsers
import kotlinx.serialization.json.*
import models.*
import models.Errors.LogicError.LOGIC_OPERATION_NOT_ALLOWED
import models.Errors.errorResponse
import operations.OperationHandler
import utils.decodeJsonObjectToObj
import utils.encodeObjToJsonObject
import utils.parseStringToJsonObject

class CreateVccOperation : OperationHandler {

    override fun processOperation(operation: String, receivedData: JsonObject): CommitInfo {
        val newVccData = decodeJsonObjectToObj<NewVccDTO>(receivedData)

        val userFileInfo = decodeJsonObjectToObj<UserFileInfoDTO>(
            parseStringToJsonObject(newVccData.userFile)
        )

        if (newVccData.vccAmount <= 0.0 ||
            userFileInfo.accountName != newVccData.account ||
            existingUsers[userFileInfo.accountName] == null ||
            existingUsers[userFileInfo.accountName]!!.pin != userFileInfo.accountPin ||
            newVccData.vccAmount > existingUsers[newVccData.account]!!.balance ||
            existingCards[newVccData.account] != null
        ) {
            return CommitInfo(operation, null, errorResponse(LOGIC_OPERATION_NOT_ALLOWED))
        }

        val newCard = VccDTO(
            newVccData.account,
            newVccData.vccAmount,
            "${newVccData.account}_${existingUsers[newVccData.account]!!.vccSeqNumber}.card"
        )

        val jsonResponse = encodeObjToJsonObject(newCard)
        return CommitInfo(operation, jsonResponse, ResponseDTO(true, jsonResponse))
    }

    override fun commit(data: JsonObject) {
        val newCard = decodeJsonObjectToObj<VccDTO>(data)
        existingCards[newCard.account] = newCard
        println(newCard)
    }
}