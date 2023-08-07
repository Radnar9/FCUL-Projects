import inputs.validateArgs
import models.Errors.LogicError.CODE
import models.Errors.LogicError.INVALID_ARGUMENTS
import models.Errors.LogicError.INVALID_OPERATION
import operations.*
import org.bouncycastle.jce.provider.BouncyCastleProvider
import sockets.OutChannel
import sockets.SecureChannel
import sun.misc.Signal
import utils.printlnErr
import java.lang.Exception
import java.security.Security
import kotlin.system.exitProcess

enum class MODE {
    SHOPPING, BANK, FAILED
}

enum class OPERATION {
    CREATE_ACCOUNT,
    CASH_DEPOSIT,
    CREATE_VCC,
    GET_BALANCE,
    WITHDRAW_AMOUNT,
    UNKNOWN
}

data class OperationMode(val operation: OPERATION, val mode: MODE)


class MBeC {
    fun init(args: Array<String>): OperationMode {
        return try {
            val optionsInserted = validateArgs(args)
            if (optionsInserted.isEmpty()) {
                printlnErr(INVALID_ARGUMENTS)
                exitProcess(CODE)
            }

            val operation = validateOperations(optionsInserted)
            if (operation == OPERATION.UNKNOWN) {
                printlnErr(INVALID_OPERATION)
                exitProcess(CODE)
            }

            if (operation == OPERATION.WITHDRAW_AMOUNT) {
                StoreSetup().main(args)
                OperationMode(operation, MODE.SHOPPING)
            } else {
                BankSetup().main(args)
                OperationMode(operation, MODE.BANK)
            }

        } catch (e: Exception) {
            OperationMode(OPERATION.UNKNOWN, MODE.FAILED)
        }
    }
}

fun main(args: Array<String>) {
    Signal.handle(Signal("INT")) { exitProcess(0) }
    val operationMode = MBeC().init(args)
    Security.addProvider(BouncyCastleProvider())

    when (operationMode.mode) {
        MODE.BANK -> bankCommunication(operationMode.operation)
        MODE.SHOPPING -> storeCommunication()
        else -> {
            printlnErr(INVALID_OPERATION)
            exitProcess(CODE)
        }
    }
}

fun storeCommunication() {
    val outChannel = OutChannel(
        globalStoreConfiguration.storeIp,
        globalStoreConfiguration.storePort,
    )
    withdrawAmount(outChannel)
}

fun bankCommunication(operation: OPERATION) {
    val secureChannel = SecureChannel(
        globalBankConfiguration.bankIp,
        globalBankConfiguration.bankPort,
        globalBankConfiguration.authFile
    )

    when (operation) {
        OPERATION.CREATE_ACCOUNT -> createAccount(secureChannel)
        OPERATION.CASH_DEPOSIT -> cashDeposit(secureChannel)
        OPERATION.GET_BALANCE -> getBalance(secureChannel)
        OPERATION.CREATE_VCC -> createVCC(secureChannel)
        else -> exitProcess(CODE)
    }
}