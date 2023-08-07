import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.int
import java.io.File

var globalBankConfiguration = BankConfiguration()
var globalStoreConfiguration = StoreConfiguration()

data class BankConfiguration(
    val accountName: String = "",
    val authFile: String = "bank.auth",
    val bankIp: String = "127.0.0.1",
    val bankPort: Int = 3001,
    val userFile: String = "<template>.user",
    val createAccount: Double = -1.0,
    val depositValue: Double = -1.0,
    val cardAmount: Double = -1.0,
    val currentBalance: Boolean = false
)

data class StoreConfiguration(
    val storeIp: String = "127.0.0.1",
    val storePort: Int = 5000,
    val virtualCreditFile: String = "55555_2.card",
    val shoppingValue: Double = -1.0
)

class BankSetup : CliktCommand() {

    // bank arguments
    private val accountName: String by option("-a", help = "<account>").default("")
    private val authFile: String by option("-s", help = "<auth-file>").default("bank.auth")
    private val bankIp: String by option("-i", help = "<ip-bank-address>").default("127.0.0.1")
    private val bankPort: Int by option("-p", help = "<bk-port>").int().default(3000)
    private val userFile: String by option("-u", help = "<user-file>").default("<template>.user")

    // bank operations
    private val createAccount: Double by option("-n", help = "<balance>").double().default(-1.0)
    private val depositValue: Double by option("-d", help = "<amount>").double().default(-1.0)
    private val cardAmount: Double by option("-c", help = "<amount>").double().default(-1.0)
    private val currentBalance by option("-g").flag()

    override fun run() {
        globalBankConfiguration = BankConfiguration(
            accountName,
            authFile,
            bankIp,
            bankPort,
            userFile.replace("<template>", accountName),
            createAccount,
            depositValue,
            cardAmount,
            currentBalance
        )
    }
}

class StoreSetup : CliktCommand() {

    // store arguments
    private val storeIp: String by option("-i", help = "<ip-store-address>").default("127.0.0.1")
    private val storePort: Int by option("-p", help = "<st-port>").int().default(5000)
    private val virtualCreditFile: String by option("-v", help = "<virtual-credit-card-file>").default("")

    // store operations
    private val shoppingValue: Double by option("-m", help = "<shopping-value>").double().default(-1.0)

    override fun run() {
        var virtualCreditCard = virtualCreditFile
        if (virtualCreditCard.isEmpty()) virtualCreditCard = getVirtualCreditCardFile()
        globalStoreConfiguration = StoreConfiguration(storeIp, storePort, virtualCreditCard, shoppingValue)
    }

    private fun getVirtualCreditCardFile(): String {
        val directory = File(System.getProperty("user.dir"))
        val regex = """^\d+_\d+\.card$""".toRegex()
        var fileName = ""
        if (directory.exists() && directory.isDirectory) {
            directory.listFiles()?.sorted()?.forEach { file ->
                if(regex.matches(file.name))
                    fileName = file.name
            }
        }
        return fileName
    }
}
