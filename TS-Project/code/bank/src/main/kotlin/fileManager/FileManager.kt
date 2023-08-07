package fileManager

import java.io.File

fun createFile(fileName: String, content: String): Boolean {
    val file = File(fileName)

    if(file.exists()) return false

    file.createNewFile()
    file.writeText(content)
    return true
}