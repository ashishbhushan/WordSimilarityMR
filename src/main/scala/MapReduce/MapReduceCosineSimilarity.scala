package MapReduce

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.conf.*
import org.apache.hadoop.io.*
import org.apache.hadoop.mapreduce.*
import org.apache.hadoop.mapreduce.lib.input.{FileInputFormat, TextInputFormat}
import org.apache.hadoop.mapreduce.lib.output.{FileOutputFormat, TextOutputFormat}
import breeze.linalg.*

import java.io.IOException
import scala.jdk.CollectionConverters.*
import org.slf4j.LoggerFactory
import UtilitiesDir.JTokkitTokenizer.detokenize

object MapReduceCosineSimilarity {
  class MapCosineSimilarity extends Mapper[LongWritable, Text, Text, Text] {
    private val logger = LoggerFactory.getLogger(classOf[MapCosineSimilarity])
    private final val outputKey = new Text()
    private val outputValue = new Text()
    
    @throws[IOException]
    override def map(key: LongWritable, value: Text, context: Mapper[LongWritable, Text, Text, Text]#Context): Unit = {
      val (tokenId, vector) = loadEmbedding(value.toString)
      outputKey.set(tokenId.toString)
      outputValue.set(vector.toArray.mkString(","))
      context.write(outputKey, outputValue)
    }

    def loadEmbedding(line: String): (Int, DenseVector[Double]) = {
      // Parse each line, split by tab, then split the embedding vector by commas
      val parts = line.split("\t")
      val tokenId = parts(0).toInt
      val embedding = parts(1).split(",").filter(_.nonEmpty).map(_.toDouble)
      tokenId -> DenseVector(embedding)
    }
  }

  class ReduceCosineSimilarity extends Reducer[Text, Text, Text, Text] {
    private var embeddings: Map[Int, DenseVector[Double]] = Map()

    @throws[IOException]
    override def reduce(key: Text, values: java.lang.Iterable[Text], context: Reducer[Text, Text, Text, Text]#Context): Unit = {
      // Convert the Iterable to a Scala collection and process it without a for loop
      val vectorData = values.asScala.toList.map { value =>
        val parts = value.toString.split(",") // Assuming your vector is comma-separated
        val vector = DenseVector(parts.map(_.toDouble)) // Convert string parts to DenseVector
        key.toString.toInt -> vector // Create a tuple (tokenId, vector)
      }
      // Update the embeddings map with the collected vector data
      embeddings ++= vectorData.toMap // Merge new entries into the embeddings map
    }

    override def cleanup(context: Reducer[Text, Text, Text, Text]#Context): Unit = {
      // Find top N similar tokens after all values have been processed
      val topN = 5 // Set your desired value for top N
      val topSimilarTokens = findTopNSimilarTokens(embeddings, topN)

      // Write the output for each token and its top similar tokens
      topSimilarTokens.foreach { case (token, similarTokens) =>
        val tokenIAL: Array[Int] = Array(token)
        val similarTokensIAL: Array[Int] = similarTokens.toArray
        context.write(new Text(detokenize(tokenIAL)), new Text(detokenize(similarTokensIAL)))
      }
    }

    // Your existing method for finding similar tokens
    def findTopNSimilarTokens(embeddings: Map[Int, DenseVector[Double]], topN: Int): Map[Int, List[Int]] = {
      embeddings.map { case (token, vector) =>
        val similarities = embeddings.collect {
          case (otherToken, otherVector) if token != otherToken =>
            (otherToken, cosineSimilarity(vector, otherVector))
        }.toList
        val topSimilarTokens = similarities.sortBy(-_._2).take(topN).map(_._1)
        token -> topSimilarTokens
      }
    }

    // Your existing cosine similarity method
    def cosineSimilarity(v1: DenseVector[Double], v2: DenseVector[Double]): Double = {
      (v1 dot v2) / (norm(v1) * norm(v2))
    }
  }

  def runMapReduceCosineSimilarity(inputPath: String, outputPath: String): Boolean = {
    val conf = new Configuration()
    val fs = FileSystem.get(conf)
    val outputDir = new Path(outputPath)
    if (fs.exists(outputDir)) {
      println(s"Output path $outputDir exists. Deleting it.")
      fs.delete(outputDir, true)
    }
    val job = Job.getInstance(conf, "Cosine Similarity")
    if (inputPath.startsWith("hdfs")) {
    } else if (inputPath.startsWith("/cs441/")) {
      conf.set("fs.defaultFS", "hdfs://localhost:9000") // Adjust this with your HDFS host if using HDFS
    } else {
      conf.set("fs.defaultFS", "file:///")
    }
    job.setJarByClass(getClass)
    job.setMapperClass(classOf[MapCosineSimilarity]) // Set your mapper class here
    job.setReducerClass(classOf[ReduceCosineSimilarity])

    job.setMapOutputKeyClass(classOf[Text])
    job.setMapOutputValueClass(classOf[Text])

    job.setOutputKeyClass(classOf[Text])
    job.setOutputValueClass(classOf[Text])

    job.setInputFormatClass(classOf[TextInputFormat])
    job.setOutputFormatClass(classOf[TextOutputFormat[Text, Text]])

    FileInputFormat.addInputPath(job, new Path(inputPath))
    FileOutputFormat.setOutputPath(job, new Path(outputPath))

    job.waitForCompletion(true)
  }
}