package operations.handlers

import models.*

import existingUsers
import kotlinx.serialization.json.*
import models.Errors.LogicError.LOGIC_OPERATION_NOT_ALLOWED
import models.Errors.errorResponse
import operations.OperationHandler
import utils.decodeJsonObjectToObj
import utils.encodeObjToJsonObject
import utils.encodeObjToString
import utils.parseStringToJsonObject

class GetBalanceOperation : OperationHandler {

    override fun processOperation(operation: String, receivedData: JsonObject): CommitInfo {
        val getDepositOperation = decodeJsonObjectToObj<GetBalanceDTO>(receivedData)

        val userFileInfo = decodeJsonObjectToObj<UserFileInfoDTO>(
            parseStringToJsonObject(getDepositOperation.userFile)
        )

        if (userFileInfo.accountName != getDepositOperation.account ||
            existingUsers[userFileInfo.accountName] == null ||
            existingUsers[userFileInfo.accountName]!!.pin != userFileInfo.accountPin
        ) {
            return CommitInfo(operation, null, errorResponse(LOGIC_OPERATION_NOT_ALLOWED))
        }

        val response = BalanceDTO(getDepositOperation.account, existingUsers[getDepositOperation.account]!!.balance)

        val json = encodeObjToJsonObject(response)
        return CommitInfo(operation, json, ResponseDTO(true, json))
    }

    override fun commit(data: JsonObject) {
        println(decodeJsonObjectToObj<BalanceDTO>(data))
    }
}