package DataProcessor

import java.io.IOException
import org.apache.hadoop.fs.{FileSystem, Path}
import DataProcessor.FileWrite.*

object Sharder {

    def shardFile(inputFile: String, outputDir: String, linesPerShard: Int, sharderFileSystem: FileSystem): Unit = {
        val flag = true
        try {
            val lines = inputFile.split("\\r?\\n").toArray

            // Filter out blank lines (empty or containing only whitespace)
            val nonBlankLines = lines.filter(_.trim.nonEmpty)

            // Group the lines into chunks (shards) of the specified size
            val shards = nonBlankLines.grouped(linesPerShard).toArray

            // Write each shard to a separate file in HDFS
            shards.zipWithIndex.foreach { case (shardLines, shardIndex) =>
                val shardFilePath = new Path(s"$outputDir/shard_${shardIndex + 1}.txt")
                writeToFile(shardLines, shardFilePath, sharderFileSystem, flag)
            }
        } catch {
            case e: IOException =>
                println(s"An error occurred: ${e.getMessage}")
        }
    }
}