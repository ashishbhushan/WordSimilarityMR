package DataProcessor

import java.io.{BufferedWriter, OutputStreamWriter, IOException}
import java.nio.charset.StandardCharsets
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import DataProcessor.DataSharder.*
import DataProcessor.FileWrite.*

object Sharder {

    def shardFile(inputFile: String, outputDir: String, linesPerShard: Int, hadoopConfig: Configuration): Unit = {
        val flag = true
        try {
            val lines = inputFile.split("\\r?\\n").toArray

            // Filter out blank lines (empty or containing only whitespace)
            val nonBlankLines = lines.filter(_.trim.nonEmpty)

            // Group the lines into chunks (shards) of the specified size
            val shards = nonBlankLines.grouped(linesPerShard).toArray

            // Initialize the Hadoop filesystem
            val fs = FileSystem.get(hadoopConfig)

            // Write each shard to a separate file in HDFS
            shards.zipWithIndex.foreach { case (shardLines, shardIndex) =>
                val shardFilePath = new Path(s"$outputDir/shard_${shardIndex + 1}.txt")
                writeToFile(shardLines, shardFilePath, fs, flag)
                //                shardLines.foreach(line => writeToFile(line, shardFilePath, fs))
                //                val outputStream = fs.create(shardFilePath, true)
                //                val writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))
                //                try {
                //                    shardLines.foreach { line =>
                //                        writer.write(shardData(line))
                //                        writer.newLine()
                //                    }
                //                    println(s"Shard ${shardIndex + 1} written to $shardFilePath")
                //                } finally {
                //                    writer.close()
                //                }
            }
        } catch {
            case e: IOException =>
                println(s"An error occurred: ${e.getMessage}")
        }
    }
}