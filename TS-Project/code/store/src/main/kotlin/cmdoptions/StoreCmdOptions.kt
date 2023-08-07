package cmdoptions

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import inputs.OPTION

class StoreCmdOptions: CliktCommand() {

    val storePort: Int by option(OPTION.STORE_PORT.option, help = "<st-port>").int().default(5000)
    val authFile: String by option(OPTION.AUTH_FILE.option, help = "<auth-file>").default("bank.auth")

    override fun run() {}
}