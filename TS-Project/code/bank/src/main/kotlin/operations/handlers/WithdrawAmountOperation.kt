package operations.handlers

import existingCards
import existingUsers
import io.ktor.util.date.*
import kotlinx.serialization.json.*
import models.*
import models.Errors.LogicError.LOGIC_OPERATION_NOT_ALLOWED
import models.Errors.errorResponse
import operations.OperationHandler
import security.decrypt
import security.retrieveEncryptedMsg
import security.retrieveIv
import utils.base64Decoder
import utils.decodeJsonObjectToObj
import utils.encodeObjToJsonObject
import utils.parseStringToJsonObject

class WithdrawAmountOperation : OperationHandler {

    companion object { private const val LIMIT_TIMESTAMP: Long = 1000 } // Milliseconds

    override fun processOperation(operation: String, receivedData: JsonObject): CommitInfo {
        val shoppingData = decodeJsonObjectToObj<WithdrawAmountDTO>(receivedData)
        val accountName = getAccountNameFromFileName(shoppingData.vccName)

        if (existingUsers[accountName] == null) return CommitInfo(
            operation,
            null,
            errorResponse(LOGIC_OPERATION_NOT_ALLOWED)
        )

        val encryptedMsg = base64Decoder(shoppingData.encryptedContent)
        val decryptedData = decrypt(
            retrieveEncryptedMsg(encryptedMsg),
            existingUsers[accountName]!!.key,
            retrieveIv(encryptedMsg)
        ).toString(Charsets.UTF_8)

        val vccFileInfo = decodeJsonObjectToObj<WithdrawAmountEncryptedInfoDTO>(parseStringToJsonObject(decryptedData))
        verifyTimestamp(vccFileInfo.timestamp)

        val name = vccFileInfo.vccContent.account
        val vccNumber = vccFileInfo.vccContent.vccNumber
        val amount = vccFileInfo.purchaseAmount

        if (amount <= 0.0 ||
            existingCards[name] == null ||
            existingUsers[name] == null ||
            vccNumber != existingUsers[name]!!.vccSeqNumber ||
            amount > existingCards[name]!!.vccAmount
        ) {
            return CommitInfo(operation, null, errorResponse(LOGIC_OPERATION_NOT_ALLOWED))
        }

        val json = encodeObjToJsonObject(WithdrawAmountResponseDTO(shoppingData.vccName, amount))
        return CommitInfo(operation, parseStringToJsonObject(decryptedData), ResponseDTO(true, json))
    }

    override fun commit(data: JsonObject) {
        val vccFileInfo = decodeJsonObjectToObj<WithdrawAmountEncryptedInfoDTO>(data)

        val name = vccFileInfo.vccContent.account
        val amount = vccFileInfo.purchaseAmount

        existingCards[name] = null
        existingUsers[name]!!.balance -= amount
        existingUsers[name]!!.vccSeqNumber++

        println(vccFileInfo.vccContent)
    }

    private fun getAccountNameFromFileName(fileName: String) = fileName.split("_")[0]

    private fun verifyTimestamp(timestamp: Long) {
        if(getTimeMillis() - timestamp > LIMIT_TIMESTAMP) throw Exception(Errors.LogicError.MAX_DELAY_TIME_EXPIRED)
    }
}
