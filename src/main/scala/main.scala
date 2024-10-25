import UtilitiesDir.Utilities
import UtilitiesDir.Sharder.*
import UtilitiesDir.ShardsToTokens.*
import MapReduce.MapReduceTokenizer
import MapReduce.MapReduceEmbedding
import MapReduce.MapReduceCosineSimilarity
import UtilitiesDir.VocabStatistics.*

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import scala.io.Source
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object main {

  def main(args: Array[String]): Unit = {

    val inputFilePath = args(0)
    val outputDir = args(1)

    val confmain = new Configuration()
    val fs = FileSystem.get(confmain)
    //noinspection DuplicatedCode
    if (inputFilePath.startsWith("hdfs")) {
    } else if (inputFilePath.startsWith("/cs441/")) {
      confmain.set("fs.defaultFS", "hdfs://localhost:9000") // Adjust this with your HDFS host if using HDFS
    } else {
      confmain.set("fs.defaultFS", "file:///")
    }

    val inputPath = new Path(inputFilePath)
    val inputStream = fs.open(inputPath)
    val lines = Source.fromInputStream(inputStream, "UTF-8").getLines().mkString("\n")

//    preprocessing+ sharding+ write shards to
    val shardsOutputDir = outputDir + "/shards"
    shardFile(lines, shardsOutputDir, Utilities.linesPerShard, fs)
    inputStream.close()

    val tokensOutputDir =  outputDir + "/tokens"
    shardsToTokens(shardsOutputDir, tokensOutputDir, Utilities.linesPerShard, fs)

    val mapRedTokenOut = outputDir + "/mapRedTokenizer"
    if (MapReduceTokenizer.runMapReduceTokenizer(shardsOutputDir, mapRedTokenOut)) {
      println(s"Token MapReduce job completed successfully. Check the output at: $mapRedTokenOut")
      //get vocab.yaml file

      val partFiles: Vector[Path] = fs.listStatus(new Path(mapRedTokenOut))
        .map(_.getPath)
        .filter(_.getName.startsWith("part-r-"))
        .toVector // Convert to Vector for initial collection

      // Create a sequence of futures for processing each file concurrently
      val futures: Seq[Future[String]] = partFiles.map { filePath =>
        Future {
          // Open input stream and read lines
          val inputStream = fs.open(filePath)
          try {
            Source.fromInputStream(inputStream, "UTF-8").getLines().mkString("\n") // Read file contents
          } finally {
            inputStream.close() // Ensure the stream is closed
          }
        }
      }
      // Combine the results of all futures and block until completion
      val combinedText: String = Await.result(Future.sequence(futures), Duration.Inf).mkString("\n")
      // Pass the combined text to the next function
      getYamlFile(combinedText, outputDir, fs)
    } else {
      println("Token MapReduce job failed.")
      System.exit(0)
    }

    val modelPath = inputPath.getParent.getParent.toString + "/model/word2vec_model.bin"
    println(s"modelPath inside main - $modelPath")
    val mapRedEmbeddingOut = outputDir + "/mapRedEmbeddingOut"
    if (MapReduceEmbedding.runMapReduceEmbedding(tokensOutputDir, mapRedEmbeddingOut, modelPath)) {
      println(s"Vector Embeddings MapReduce job completed successfully. Check the output at: $mapRedEmbeddingOut")
    } else {
      println("Vector Embeddings MapReduce job failed.")
      System.exit(0)
    }

    val mapRedCosineSimilarityOut = outputDir + "/mapRedCosineSimilarity"
    if (MapReduceCosineSimilarity.runMapReduceCosineSimilarity(mapRedEmbeddingOut, mapRedCosineSimilarityOut)) {
      println(s"Cosine Similarity MapReduce job completed successfully. Check the output at: $mapRedCosineSimilarityOut")
    } else {
      println("Cosine Similarity MapReduce job failed.")
      System.exit(0)
    }

    fs.close()
  }
}
