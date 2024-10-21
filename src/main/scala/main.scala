import UtilitiesDir.Utilities
import UtilitiesDir.Sharder.*
import UtilitiesDir.ShardsToTokens.*
import MapReduce.MapReduceTokenizer
import MapReduce.MapReduceEmbedding
import UtilitiesDir.VocabStatistics.*

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import scala.io.Source

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
    val lines = Source.fromInputStream(inputStream).getLines().mkString("\n")
    inputStream.close()

//    preprocessing+ sharding+ write shards to
    val shardsOutputDir = outputDir + "/shards"
    shardFile(lines, shardsOutputDir, Utilities.linesPerShard, fs)

    val tokensFilePath =  outputDir + "/tokens"
    shardsToTokens(shardsOutputDir, new Path(tokensFilePath+"/tokens.txt"), fs)

    val mapRedTokenOut = outputDir + "/mapRedTokenOut"
    if (MapReduceTokenizer.runMapReduceTokenizer(shardsOutputDir, mapRedTokenOut)) {
      println(s"Token MapReduce job completed successfully. Check the output at: $mapRedTokenOut")
      //get vocab.yaml file
      val mapRedTokenOutFile = mapRedTokenOut + "/part-r-00000"
      val inputStream2 = fs.open(Path(mapRedTokenOutFile))
      val text = Source.fromInputStream(inputStream2).getLines().mkString("\n")
      getYamlFile(text, outputDir, fs)
      inputStream2.close()
    } else {
      println("Token MapReduce job failed.")
    }

    val modelPath = inputPath.getParent.getParent.toString + "/model/word2vec_model.bin"
    println(s"modelPath inside main - $modelPath")
    val mapRedEmbeddingOut = outputDir + "/mapRedEmbeddingOut"
    if (MapReduceEmbedding.runMapReduceEmbedding(tokensFilePath, mapRedEmbeddingOut, modelPath)) {
      println(s"Vector Embeddings MapReduce job completed successfully. Check the output at: $mapRedEmbeddingOut")
    } else {
      println("Vector Embeddings MapReduce job failed.")
    }
  }
}
