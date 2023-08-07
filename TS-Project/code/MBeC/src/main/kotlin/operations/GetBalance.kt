package operations

import fileManager.readFile
import globalBankConfiguration
import models.BalanceDTO
import models.Errors.LogicError.CODE
import models.Errors.LogicError.INVALID_USER_FILE
import models.GetBalanceDTO
import models.RequestDTO
import sockets.SecureChannel
import utils.*
import kotlin.system.exitProcess

fun getBalance(secureChannel: SecureChannel){

    val userFileContent = readFile(globalBankConfiguration.userFile)

    if(userFileContent.isEmpty()) {
        printlnErr(INVALID_USER_FILE)
        exitProcess(CODE)
    }

    val getBalanceJson = encodeObjToJsonObject(
        GetBalanceDTO(
            globalBankConfiguration.accountName,
            userFileContent,
        )
    )

    secureChannel.sendMessage(RequestDTO(OPERATION.GET_BALANCE.name, getBalanceJson)) {
        if (!it.success) {
            printlnErr(encodeObjToString(it.data))
            exitProcess(CODE)
        }
        println(decodeJsonObjectToObj<BalanceDTO>(it.data))
    }
}