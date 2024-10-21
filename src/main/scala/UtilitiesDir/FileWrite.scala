package UtilitiesDir

import UtilitiesDir.DataSharder.shardData
import org.apache.hadoop.fs.{FileSystem, Path}

import java.io.{BufferedWriter, OutputStreamWriter}
import java.nio.charset.StandardCharsets

object FileWrite {
    def writeToFile(text: Array[String], filePath: Path, fwFileSystem: FileSystem, flag: Boolean): Unit = {
        val outputStream = fwFileSystem.create(filePath, true)
        val writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))
        try {
            text.foreach { line =>
                if (flag) {
                    writer.write(shardData(line))
                } else {
                    writer.write(line)
                }
                writer.newLine()
            }
            println(s"Successfully wrote to the file: $filePath")
        } finally {
            writer.close()
        }
    }
}