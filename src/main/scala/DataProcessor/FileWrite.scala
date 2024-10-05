package DataProcessor

import DataProcessor.DataSharder.shardData
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

import java.io.{BufferedWriter, IOException, OutputStreamWriter}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, StandardOpenOption}

object FileWrite {
    def writeToFile(text: Array[String], filePath: Path, fs: FileSystem, flag: Boolean): Unit = {
        val outputStream = fs.create(filePath, true)
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