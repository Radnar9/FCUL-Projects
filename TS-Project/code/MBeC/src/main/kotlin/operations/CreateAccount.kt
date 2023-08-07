package operations

import fileManager.createFile
import fileManager.existsFile
import fileManager.writeToFile
import globalBankConfiguration
import models.NewAccountDTO
import models.RequestDTO
import sockets.SecureChannel
import kotlin.system.exitProcess
import OPERATION
import models.Errors.LogicError.CODE
import models.Errors.LogicError.LOGIC_OPERATION_NOT_ALLOWED
import utils.encodeObjToJsonObject
import utils.encodeObjToString
import utils.printlnErr

fun createAccount(secureChannel: SecureChannel) {
    if (existsFile(globalBankConfiguration.userFile) || globalBankConfiguration.createAccount < 15.0) {
        printlnErr(LOGIC_OPERATION_NOT_ALLOWED)
        exitProcess(CODE)
    }

    val newAccount = NewAccountDTO(
        globalBankConfiguration.accountName,
        globalBankConfiguration.userFile,
        globalBankConfiguration.createAccount
    )

    secureChannel.sendMessage(RequestDTO(OPERATION.CREATE_ACCOUNT.name, encodeObjToJsonObject(newAccount))) {
        if (!it.success) {
            printlnErr(encodeObjToString(it.data))
            exitProcess(CODE)
        }
        createFile(globalBankConfiguration.userFile)
        writeToFile(globalBankConfiguration.userFile, encodeObjToString(it.data))

        println(newAccount)
    }
}