package operations

import fileManager.readFile
import globalStoreConfiguration
import io.ktor.util.date.*
import models.*
import models.Errors.LogicError.CODE
import models.Errors.LogicError.INVALID_WITHDRAW_AMOUNT
import models.Errors.LogicError.NON_EXISTENT_FILE
import security.encryptWithIv
import security.generateIv
import sockets.OutChannel
import utils.*
import kotlin.system.exitProcess

fun withdrawAmount(outChannel: OutChannel) {
    val vccFileContentStr = readFile(globalStoreConfiguration.virtualCreditFile)

    if (vccFileContentStr.isEmpty()) {
        printlnErr(NON_EXISTENT_FILE)
        exitProcess(CODE)
    }

    if (globalStoreConfiguration.shoppingValue < 0) {
        printlnErr(INVALID_WITHDRAW_AMOUNT)
        exitProcess(CODE)
    }

    val vccFileContent = decodeStringToObj<NewVccCreatedDto>(vccFileContentStr)
    val dataToEncrypt = WithdrawAmountEncryptedInfoDTO(
        vccFileContent,
        globalStoreConfiguration.shoppingValue,
        getTimeMillis(),
    ).run { encodeObjToString(this) }

    val iv = generateIv()
    val encryptedData = encryptWithIv(dataToEncrypt.toByteArray(), getKeyFromUserFile(vccFileContent.userFile), iv)

    val shoppingReq = encodeObjToString(
        WithdrawAmountDTO(
            globalStoreConfiguration.virtualCreditFile,
            base64Encoder(encryptedData),
        )
    )

    outChannel.sendMessage(
        RequestDTO(
            OPERATION.WITHDRAW_AMOUNT.name,
            parseStringToJsonObject(shoppingReq)
        )
    ) {
        if (!it.success) {
            printlnErr(encodeObjToString(it.data))
            exitProcess(CODE)
        }

        println(decodeJsonObjectToObj<WithdrawAmountResponseDTO>(it.data))
    }
}

private fun getKeyFromUserFile(filename: String) = base64Decoder(
    decodeJsonObjectToObj<UserFileInfoDTO>(
        parseStringToJsonObject(readFile(filename))
    ).accountKey
)