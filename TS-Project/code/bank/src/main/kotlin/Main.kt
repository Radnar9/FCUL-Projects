import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import fileManager.createFile
import inputs.OPTION
import inputs.validateArgs
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import models.*
import models.Errors.LogicError.CODE
import models.Errors.LogicError.INVALID_ARGUMENTS
import models.Errors.LogicError.INVALID_AUTH_FILE
import models.Errors.LogicError.INVALID_OPERATION
import models.Errors.errorResponse
import operations.OperationInterpreter
import org.bouncycastle.jce.provider.BouncyCastleProvider
import security.generateKey
import sockets.SecureChannel
import sun.misc.Signal
import utils.base64Encoder
import utils.encodeObjToString
import utils.printlnErr
import java.security.Security
import kotlin.collections.HashMap
import kotlin.system.exitProcess


var existingUsers = HashMap<String, UserAccountInfo>()
var existingCards = HashMap<String, VccDTO?>()
val authFileKey = generateKey()

class Bank : CliktCommand() {
    val bankPort: Int by option(OPTION.BANK_PORT.option, help = "<bank-port>").int().default(3000)
    val authFile: String by option(OPTION.AUTH_FILE.option, help = "<auth-file>").default("bank.auth")
    val bankIp = "127.0.0.1"

    override fun run() {}
}

fun main(args: Array<String>) {
    Signal.handle(Signal("INT")) { exitProcess(0) }
    if (!validateArgs(args)) {
        printlnErr(INVALID_ARGUMENTS)
        exitProcess(CODE)
    }
    val bankOptions = Bank().apply { main(args) }

    Security.addProvider(BouncyCastleProvider())

    val authFile = AuthFileInfoDTO(base64Encoder(authFileKey) , bankOptions.bankIp, bankOptions.bankPort)
    if (!createFile(bankOptions.authFile, encodeObjToString(authFile))) {
        printlnErr(INVALID_AUTH_FILE)
        exitProcess(CODE)
    }
    println("created")

    val operationInterpreter = OperationInterpreter()
    SecureChannel(ip = bankOptions.bankIp, port = bankOptions.bankPort).waitForMessage(
        { request ->
            val receivedData = Json.decodeFromString<RequestDTO>(request)
            try {
                val opHandler = operationInterpreter.getOperationHandler(receivedData.operation) ?: throw Exception()
                opHandler.processOperation(receivedData.operation, receivedData.data)
            } catch (e: Exception) {
                printlnErr(INVALID_OPERATION)
                CommitInfo(receivedData.operation, null, errorResponse(INVALID_OPERATION))
            }
        },
        { response ->
            val opHandler = operationInterpreter.getOperationHandler(response.operation)
            opHandler!!.commit(response.commitData!!)
        }
    )
}