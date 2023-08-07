package operations

import fileManager.readFile
import globalBankConfiguration
import models.DepositDTO
import models.Errors.LogicError.CODE
import models.Errors.LogicError.INVALID_DEPOSIT_AMOUNT
import models.Errors.LogicError.INVALID_USER_FILE
import models.NewDepositDTO
import models.RequestDTO
import sockets.SecureChannel
import utils.*
import kotlin.system.exitProcess


fun cashDeposit(secureChannel: SecureChannel) {
    if (globalBankConfiguration.depositValue <= 0) {
        printlnErr(INVALID_DEPOSIT_AMOUNT)
        exitProcess(CODE)
    }

    val userFileContent = readFile(globalBankConfiguration.userFile)

    if (userFileContent.isEmpty()) {
        printlnErr(INVALID_USER_FILE)
        exitProcess(CODE)
    }

    val newDepositJson = encodeObjToJsonObject(
        NewDepositDTO(
            globalBankConfiguration.accountName,
            userFileContent,
            globalBankConfiguration.depositValue
        )
    )

    secureChannel.sendMessage(RequestDTO(OPERATION.CASH_DEPOSIT.name, newDepositJson)) {
        if (!it.success) {
            printlnErr(encodeObjToString(it.data))
            exitProcess(CODE)
        }
        println(decodeJsonObjectToObj<DepositDTO>(it.data))
    }
}