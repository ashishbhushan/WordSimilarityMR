package MapReduce

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.conf.*
import org.apache.hadoop.io.*
import org.apache.hadoop.mapreduce.*
import org.apache.hadoop.mapreduce.lib.input.{FileInputFormat, TextInputFormat}
import org.apache.hadoop.mapreduce.lib.output.{FileOutputFormat, TextOutputFormat}

import java.io.{File, FileOutputStream, IOException, InputStream}
import ModelDir.Word2VecModel.loadSentences
import UtilitiesDir.EmbeddingGenerator.*
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.models.word2vec.Word2Vec
import org.slf4j.LoggerFactory

import scala.util.Using
import scala.jdk.CollectionConverters.*

object MapReduceEmbedding:
  class MapEmbedding extends Mapper[LongWritable, Text, Text, Text] {
    private val logger = LoggerFactory.getLogger(classOf[MapEmbedding])
    private val outputKey = new Text()
    private val outputValue = new Text()

    var word2Vec: Word2Vec = _

    override def setup(context: Mapper[LongWritable, Text, Text, Text]#Context): Unit = {
      // Get the model path from job configuration
      val modelPath = context.getConfiguration.get("word2vec.model.path")
      val fsSetup = FileSystem.get(context.getConfiguration)
      println(s"model path ashish - $modelPath")
      val hdfsModelPath = new Path(modelPath)
      if (!fsSetup.exists(hdfsModelPath)) {
        throw new RuntimeException(s"Model file does not exist at $hdfsModelPath")
      }
      println(hdfsModelPath)
      val modelInputStream: InputStream = fsSetup.open(hdfsModelPath)

      // Create a temporary local file to store the model
      val tempModelFile = File.createTempFile("word2vec_modelTemp", ".bin")

      try {
        // Write the model from HDFS to the temporary local file
        Using.resource(new FileOutputStream(tempModelFile)) { outputStream =>
          val buffer: Array[Byte] = new Array(4096)   // Specify buffer size here
          LazyList.continually(modelInputStream.read(buffer)).takeWhile(_ != -1).foreach(outputStream.write(buffer, 0, _))
        }
        // Load the Word2Vec model from the temporary file
        word2Vec = WordVectorSerializer.readWord2VecModel(tempModelFile)
        // Now you can use word2Vec for further processing
      } finally {
        // Close the input stream and delete the temporary file
        modelInputStream.close()
        tempModelFile.delete() // Clean up the temporary file
      }
    }

    @throws[IOException]
    override def map(key: LongWritable, value: Text, context: Mapper[LongWritable, Text, Text, Text]#Context): Unit = {
      val lines: List[String] = value.toString.split("\n").toList
      val allSentences = loadSentences(lines)
      val embeddings = generateEmbeddings(word2Vec, allSentences)
      val uniqueEmbeddings: Map[Int, Array[Double]] = embeddings
        .groupBy(_._1) // Group by token ID
        .map { case (tokenId, embeddings) =>
          val firstEmbedding = embeddings.head // Or use a strategy to combine embeddings
          (tokenId, firstEmbedding._2) // Take the first embedding
        }
      uniqueEmbeddings.foreach { case (tokenId, embedding) =>
        val embeddingStr = embedding.mkString(", ")
        outputKey.set(tokenId.toString) // Set the token ID as the key
        outputValue.set(embeddingStr) // Set the embedding as the value
        context.write(outputKey, outputValue) // Emit the token ID and its embedding
      }
    }
  }

  class ReduceEmbedding extends Reducer[Text, Text, Text, Text] {
    override def reduce(key: Text, values: java.lang.Iterable[Text], context: Reducer[Text, Text, Text, Text]#Context): Unit = {
      // Since your mapper already outputs unique token IDs with their embeddings,
      // we just need to take the first value provided by the mapper for each key.
      val valueIterator = values.asScala.iterator
      if (valueIterator.hasNext) {
        // Emit the key and the first value associated with it
        context.write(key, valueIterator.next())
      }
    }
  }

  def runMapReduceEmbedding(inputPath: String, outputPath: String, modelPath: String): Boolean = {
    val conf = new Configuration()
    val fs = FileSystem.get(conf)
    val outputDir = new Path(outputPath)
    if (fs.exists(outputDir)) {
      println(s"Output path $outputDir exists. Deleting it.")
      fs.delete(outputDir, true)
    }
    if (inputPath.startsWith("hdfs")) {
      println("executed hdfs")
    } else if (inputPath.startsWith("/cs441/")) {
      conf.set("fs.defaultFS", "hdfs://localhost:9000") // Adjust this with your HDFS host if using HDFS
      println("executed cs441")
      println(modelPath)
      conf.set("word2vec.model.path", modelPath)
      println("set was executed")
    } else {
      conf.set("fs.defaultFS", "file:///")
      println("executed local")
      println(modelPath)
      conf.set("word2vec.model.path", modelPath)
      println("set was executed")
    }
    val job = Job.getInstance(conf, "Vector Embeddings")
    job.setJarByClass(getClass)
    job.setMapperClass(classOf[MapEmbedding]) // Set your mapper class here
    job.setReducerClass(classOf[ReduceEmbedding])

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