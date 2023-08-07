package operations

import fileManager.createFile
import fileManager.readFile
import fileManager.writeToFile
import globalBankConfiguration
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import models.*
import models.Errors.LogicError.CODE
import models.Errors.LogicError.INVALID_USER_FILE
import models.Errors.LogicError.LOGIC_OPERATION_NOT_ALLOWED
import sockets.SecureChannel
import utils.decodeJsonObjectToObj
import utils.encodeObjToString
import utils.printlnErr
import kotlin.system.exitProcess

fun createVCC(secureChannel: SecureChannel) {

    if (globalBankConfiguration.cardAmount <= 0) {
        printlnErr(LOGIC_OPERATION_NOT_ALLOWED)
        exitProcess(CODE)
    }

    val userFileContent = readFile(globalBankConfiguration.userFile)

    if (userFileContent.isEmpty()) {
        printlnErr(INVALID_USER_FILE)
        exitProcess(CODE)
    }

    val newVCCJson = encodeObjToString(
        NewVccDTO(
            globalBankConfiguration.accountName,
            userFileContent,
            globalBankConfiguration.cardAmount
        )
    )

    secureChannel.sendMessage(
        RequestDTO(
            OPERATION.CREATE_VCC.name,
            Json.parseToJsonElement(newVCCJson).jsonObject
        )
    ) {
        if (!it.success) {
            printlnErr(encodeObjToString(it.data))
            exitProcess(CODE)
        } else {
            val vccCreated = decodeJsonObjectToObj<NewVccInfoDto>(it.data)

            val vccNumber = vccCreated.vccFile.split("_")[1].split(".")[0].toInt()

            createFile(vccCreated.vccFile)

            writeToFile(vccCreated.vccFile, encodeObjToString(
                    NewVccCreatedDto(
                        globalBankConfiguration.userFile,
                        vccCreated.account,
                        vccNumber,
                        vccCreated.vccAmount
                    )
                )
            )
            println(decodeJsonObjectToObj<NewVccInfoDto>(it.data))
        }
    }
}