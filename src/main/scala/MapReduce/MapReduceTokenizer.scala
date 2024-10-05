package MapReduce

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.conf.*
import org.apache.hadoop.io.*
import org.apache.hadoop.mapred.*

import java.io.IOException
import java.util
import scala.jdk.CollectionConverters.*
import Tokenizer.JTokkitTokenizer.*

object MapReduceTokenizer:
    //noinspection ScalaWeakerAccess
    class MapTokenizer extends MapReduceBase with Mapper[LongWritable, Text, Text, IntWritable]:
        private final val one = new IntWritable(1)
        private val words = new Text()

        @throws[IOException]
        override def map(key: LongWritable, value: Text, output: OutputCollector[Text, IntWritable], reporter: Reporter): Unit =
            val line: String = value.toString
            line.split(" ").foreach { word =>
                val tokens = tokenize(word).mkString(", ")
                words.set(s"$word, [$tokens]")
                //                println(words)
                output.collect(words, one)
            }

    //noinspection ScalaWeakerAccess
    class ReduceTokenizer extends MapReduceBase with Reducer[Text, IntWritable, Text, IntWritable]:
        override def reduce(key: Text, values: util.Iterator[IntWritable], output: OutputCollector[Text, IntWritable], reporter: Reporter): Unit =
            val sum = values.asScala.reduce((valueOne, valueTwo) => new IntWritable(valueOne.get() + valueTwo.get()))
            output.collect(key, new IntWritable(sum.get()))

    def runMapReduceTokenizer(inputPath: String, outputPath: String): Boolean = {
        val conftemp = new Configuration()
        val fs = FileSystem.get(conftemp)
        val outputDir = new Path(outputPath)
        if (fs.exists(outputDir)) {
            println(s"Output path $outputDir exists. Deleting it.")
            fs.delete(outputDir, true)
        }
        val conf: JobConf = new JobConf(this.getClass)
        conf.setJobName("WordCount")
        //noinspection DuplicatedCode
        if (inputPath.startsWith("hdfs")) {
        } else if (inputPath.startsWith("/cs441/input")) {
            conf.set("fs.defaultFS", "hdfs://localhost:9000") // Adjust this with your HDFS host if using HDFS
        } else {
            conf.set("fs.defaultFS", "file:///")
        }
        conf.set("mapreduce.job.maps", "1")
        conf.set("mapreduce.job.reduces", "1")
        conf.setOutputKeyClass(classOf[Text])
        conf.setOutputValueClass(classOf[IntWritable])
        conf.setMapperClass(classOf[MapTokenizer])
        conf.setCombinerClass(classOf[ReduceTokenizer])
        conf.setReducerClass(classOf[ReduceTokenizer])
        conf.setInputFormat(classOf[TextInputFormat])
        conf.setOutputFormat(classOf[TextOutputFormat[Text, IntWritable]])
        FileInputFormat.setInputPaths(conf, new Path(inputPath))
        FileOutputFormat.setOutputPath(conf, new Path(outputPath))
        val jobSuccess = JobClient.runJob(conf)
        val jobStatus = jobSuccess.isSuccessful
        if (jobStatus) {
            println("Job completed successfully!")
        } else {
            println("Job failed.")
        }
        jobStatus
    }