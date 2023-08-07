package filemanager

import java.io.File
import java.nio.file.Files

fun existsFile(fileName: String): Boolean {
    return File(fileName).exists()
}

fun createFile(fileName: String) {
    val file = File(fileName)
    file.createNewFile()
}

fun writeToFile(fileName: String, content: String): Boolean {
    val file = File(fileName)

    if (!file.exists()) return false

    file.writeText(content)
    return true
}


fun deleteFile(fileName: String): Boolean {
    val file = File(fileName)

    if (!file.exists()) return false

    file.delete()
    return true
}

fun readFile(fileName: String): String {
    val file = File(fileName)

    if (!file.exists()) return ""

    return file.bufferedReader().use { it.readLine() }
}
