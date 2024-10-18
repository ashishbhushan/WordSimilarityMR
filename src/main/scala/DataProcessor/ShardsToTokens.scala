package DataProcessor

import org.apache.hadoop.fs.{FileSystem, Path}
import Tokenizer.JTokkitTokenizer.*
import java.io.{BufferedWriter, IOException, OutputStreamWriter}
import java.nio.charset.StandardCharsets
import scala.io.Source

object ShardsToTokens {

  def shardsToTokens(inputDir: String, outputFile: Path, sttFileSystem: FileSystem): Unit = {
    val allTokens: Array[Array[Int]] = Array.empty[Array[Int]]
    val outputStream = sttFileSystem.create(outputFile, true)
    val writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))
    try {
      val shardFilePaths = sttFileSystem.listStatus(new Path(inputDir)).map(_.getPath).filter(_.getName.endsWith(".txt"))
      shardFilePaths.foreach { shardFilePath =>
        val inputStream = sttFileSystem.open(shardFilePath)
        val lines = Source.fromInputStream(inputStream).getLines().mkString("\n")
        println(s"Working on - $shardFilePath to extract tokens")
        lines.split("\n").foreach { line =>
          writer.write("[" + tokenize(line).mkString(",") + "]")
          writer.newLine()
        }
        inputStream.close()
      }
    } catch {
      case e: IOException =>
        println(s"An error occurred: ${e.getMessage}")
    } finally {
      writer.close()
    }
  }
}
