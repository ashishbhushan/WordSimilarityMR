import UtilitiesDir.Utilities.*
import DataProcessor.Sharder.*
import MapReduce.MapReduceTokenizer
import VocabStatistics.VocabStatistics.*

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import scala.io.Source

object main {

  def main(args: Array[String]): Unit = {

    val inputFilePath = args(0)
    val outputDir = args(1)
    val confmain = new Configuration()
    val fs = FileSystem.get(confmain)
    if (inputFilePath.startsWith("hdfs")) {
    } else if (inputFilePath.startsWith("/cs441/input")) {
      confmain.set("fs.defaultFS", "hdfs://localhost:9000") // Adjust this with your HDFS host if using HDFS
    } else {
      confmain.set("fs.defaultFS", "file:///")
    }
    val inputPath = new Path(inputFilePath)

    val inputStream = fs.open(inputPath)
    val lines = Source.fromInputStream(inputStream).getLines().mkString("\n")

    //preprocessing+ sharding+ write shards to
    val shardsOutputDir = outputDir+"/shards"

    shardFile(lines, shardsOutputDir, linesPerShard, confmain)
    inputStream.close()

    val mapRedTokenOut = outputDir+"/mapRedTokenOut"

    if (MapReduceTokenizer.runMapReduceTokenizer(shardsOutputDir, mapRedTokenOut)) {
      println(s"Token MapReduce job completed successfully. Check the output at: $mapRedTokenOut")

      //get vocab.yaml file
      val mapRedTokenOutFile = mapRedTokenOut + "/part-00000"
      val inputStream2 = fs.open(Path(mapRedTokenOutFile))
      val text = Source.fromInputStream(inputStream2).getLines().mkString("\n")
      getYamlFile(text, outputDir, fs)
      inputStream2.close()
    } else {
      println("Token MapReduce job failed.")
    }
  }
}
