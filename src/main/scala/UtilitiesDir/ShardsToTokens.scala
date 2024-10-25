package UtilitiesDir

import org.apache.hadoop.fs.{FileSystem, Path}
import JTokkitTokenizer.*

import java.io.IOException
import scala.io.Source
import FileWrite.*

import scala.annotation.tailrec

object ShardsToTokens {

  def shardsToTokens(inputDir: String, outputDir: String, linesPerShard: Int, sttFileSystem: FileSystem): Unit = {
    @tailrec
    def processShardLines(shardFilePaths: Seq[Path], shardLines: Array[String], shardIndex: Int, currentLineCount: Int): Unit = {
      shardFilePaths match {
        case head +: tail =>
          // Open the file (head is now correctly inferred as Path)
          val inputStream = sttFileSystem.open(head)
          val lines = Source.fromInputStream(inputStream, "UTF-8").getLines().toArray

          println(s"Working on - $head to extract tokens")

          val (remainingLines, nextShardLines, nextLineCount, nextShardIndex) =
            lines.foldLeft((shardLines, Array.empty[String], currentLineCount, shardIndex)) {
              case ((accShardLines, accLines, lineCount, shardIdx), line) =>
                val tokenizedLine = "[" + tokenize(line).mkString(",") + "]"
                val updatedLines = accShardLines :+ tokenizedLine

                if (lineCount + 1 >= linesPerShard) {
                  val shardFilePath = new Path(s"$outputDir/token_${shardIdx + 1}.txt")
                  writeToFile(updatedLines, shardFilePath, sttFileSystem, flag = false)
                  (Array.empty[String], Array.empty[String], 0, shardIdx + 1) // Reset for the next shard
                } else {
                  (updatedLines, accLines, lineCount + 1, shardIdx)
                }
            }
          inputStream.close()
          // Recurse to process remaining files and lines
          processShardLines(tail, remainingLines, nextShardIndex, nextLineCount)
        case Nil =>
          // Write the last shard if there are remaining lines
          if (shardLines.nonEmpty) {
            val shardFilePath = new Path(s"$outputDir/token_${shardIndex + 1}.txt")
            writeToFile(shardLines, shardFilePath, sttFileSystem, flag = false)
          }
      }
    }

    try {
      // Ensure shardFilePaths is a Seq[Path]
      val shardFilePaths: Seq[Path] = sttFileSystem.listStatus(new Path(inputDir)).map(_.getPath).filter(_.getName.endsWith(".txt"))
      processShardLines(shardFilePaths, Array.empty[String], 0, 0) // Start the recursive processing
    } catch {
      case e: IOException =>
        println(s"An error occurred: ${e.getMessage}")
    }
  }
}
