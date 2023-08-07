package operations.handlers

import existingUsers
import kotlinx.serialization.json.*
import models.*
import models.Errors.LogicError.LOGIC_OPERATION_NOT_ALLOWED
import models.Errors.errorResponse
import operations.OperationHandler
import security.generateKey
import utils.base64Encoder
import utils.decodeJsonObjectToObj
import utils.encodeObjToJsonObject
import java.util.*

class CreateAccountOperation : OperationHandler {

    override fun processOperation(operation: String, receivedData: JsonObject): CommitInfo {
        val newAccountData = decodeJsonObjectToObj<NewAccountDTO>(receivedData)
        val accountName = newAccountData.account

        if (newAccountData.initialBalance < 15 || existingUsers[accountName] != null) {

            return CommitInfo(operation, null, errorResponse(LOGIC_OPERATION_NOT_ALLOWED))
        }

        val userAccountInfo = UserAccountInfo(
            accountName,
            generatePin(),
            newAccountData.initialBalance,
            generateKey()
        )

        val json = encodeObjToJsonObject(UserFileInfoDTO(accountName, userAccountInfo.pin, base64Encoder(userAccountInfo.key)))

        //The bank will send to the client the content of the user_file
        return CommitInfo(operation, encodeObjToJsonObject(userAccountInfo), ResponseDTO(true, json))
    }

    override fun commit(data: JsonObject) {
        val userInfo = decodeJsonObjectToObj<UserAccountInfo>(data)
        existingUsers[userInfo.accountName] = userInfo
        println(NewAccountPrint(userInfo.accountName, userInfo.balance))
    }

    private fun generatePin(): String {
        return Random().nextInt(10000).toString()
    }
}