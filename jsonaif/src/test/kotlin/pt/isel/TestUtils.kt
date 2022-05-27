package pt.isel

import java.io.File
import java.io.PrintWriter

fun setupFiles() {
    val path = "src/test/resources/"
    repeat(3) {
        val fileName = path + "Student$it.txt"
        val newText = "{nr: ${it + 48000}, name: \"Student $it\"}"
        changeFile(fileName, newText)
    }
}


fun changeFile(fileName: String, newText: String) {
    val file = File(fileName)
    val writer = PrintWriter(file)
    writer.print(newText)
    writer.close()
}