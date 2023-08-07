import cmdoptions.StoreCmdOptions
import filemanager.existsFile
import inputs.validateArgs
import models.RequestDTO
import models.Errors.LogicError.CODE
import models.Errors.LogicError.INVALID_ARGUMENTS
import models.Errors.LogicError.INVALID_OPERATION
import models.Errors.LogicError.NON_EXISTENT_FILE
import models.Errors.errorResponse
import operations.OperationInterpreter
import org.bouncycastle.jce.provider.BouncyCastleProvider
import sockets.InChannel
import sun.misc.Signal
import utils.decodeStringToObj
import utils.printlnErr
import java.security.Security
import kotlin.system.exitProcess

var authFile: String = "bank.auth"

fun main(args: Array<String>) {
    Signal.handle(Signal("INT")) { exitProcess(0) }

    if (!validateArgs(args)) {
        printlnErr(INVALID_ARGUMENTS)
        exitProcess(CODE)
    }

    val storeOptions = StoreCmdOptions().apply { main(args) }
    Security.addProvider(BouncyCastleProvider())

    if (!existsFile(storeOptions.authFile)) {
        printlnErr(NON_EXISTENT_FILE)
        exitProcess(CODE)
    }
    authFile = storeOptions.authFile

    val operationInterpreter = OperationInterpreter()
    InChannel("127.0.0.1", storeOptions.storePort).waitForMessage { request ->
        try {
            val receivedData = decodeStringToObj<RequestDTO>(request)
            val opHandler = operationInterpreter.getOperationHandler(receivedData.operation)
            val response = opHandler?.processOperation(receivedData.data) ?: throw Exception()

            response
        } catch (e: Exception) {
            printlnErr(INVALID_OPERATION)
            errorResponse(INVALID_OPERATION)
        }
    }
}
