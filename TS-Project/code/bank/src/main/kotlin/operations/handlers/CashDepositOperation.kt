package operations.handlers

import existingUsers
import kotlinx.serialization.json.*
import models.*
import models.Errors.LogicError.LOGIC_OPERATION_NOT_ALLOWED
import models.Errors.errorResponse
import operations.OperationHandler
import utils.decodeJsonObjectToObj
import utils.encodeObjToJsonObject
import utils.encodeObjToString
import utils.parseStringToJsonObject

class CashDepositOperation : OperationHandler {

    override fun processOperation(operation: String, receivedData: JsonObject): CommitInfo {
        val newDepositData = decodeJsonObjectToObj<NewDepositDTO>(receivedData)

        val userFileInfo = decodeJsonObjectToObj<UserFileInfoDTO>(
            parseStringToJsonObject(newDepositData.userFile)
        )

        if (newDepositData.depositAmount <= 0.0 ||
            userFileInfo.accountName != newDepositData.account ||
            existingUsers[userFileInfo.accountName] == null ||
            existingUsers[userFileInfo.accountName]!!.pin != userFileInfo.accountPin
        ) {
            return CommitInfo(operation, null, errorResponse(LOGIC_OPERATION_NOT_ALLOWED))
        }

        val response = DepositDTO(newDepositData.account, newDepositData.depositAmount)

        val json = encodeObjToJsonObject(response)
        return CommitInfo(operation, json, ResponseDTO(true, json))
    }

    override fun commit(data: JsonObject) {
        val depositData = decodeJsonObjectToObj<DepositDTO>(data)
        existingUsers[depositData.account]!!.balance += depositData.deposit
        println(depositData)
    }
}