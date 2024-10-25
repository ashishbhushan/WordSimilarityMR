package MapReduce

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.conf.*
import org.apache.hadoop.io.*
import org.apache.hadoop.mapreduce.*
import org.apache.hadoop.mapreduce.lib.input.{FileInputFormat, TextInputFormat}
import org.apache.hadoop.mapreduce.lib.output.{FileOutputFormat, TextOutputFormat}

import java.io.IOException
import scala.jdk.CollectionConverters.*
import UtilitiesDir.JTokkitTokenizer.*
import org.slf4j.LoggerFactory

object MapReduceTokenizer {
    class MapTokenizer extends Mapper[LongWritable, Text, Text, IntWritable] {
        private val logger = LoggerFactory.getLogger(classOf[MapTokenizer])
        private final val one = new IntWritable(1)
        private val words = new Text()

        @throws[IOException]
        override def map(key: LongWritable, value: Text, context: Mapper[LongWritable, Text, Text, IntWritable]#Context): Unit = {
            val line: String = value.toString
            line.split(" ").foreach { word =>
                val tokens = tokenize(word).mkString(", ")
                words.set(s"$word, [$tokens]")
                context.write(words, one)
            }
        }
    }

    class ReduceTokenizer extends Reducer[Text, IntWritable, Text, IntWritable] {
        @throws[IOException]
        override def reduce(key: Text, values: java.lang.Iterable[IntWritable], context: Reducer[Text, IntWritable, Text, IntWritable]#Context): Unit = {
            // Calculate the sum of the values
            val sum = values.asScala.foldLeft(0) { (acc, value) => acc + value.get() }

            // Emit the key and the computed sum
            context.write(key, new IntWritable(sum))
        }
    }

    def runMapReduceTokenizer(inputPath: String, outputPath: String): Boolean = {
        val conf = new Configuration()
        val fs = FileSystem.get(conf)
        val outputDir = new Path(outputPath)
        if (fs.exists(outputDir)) {
            println(s"Output path $outputDir exists. Deleting it.")
            fs.delete(outputDir, true)
        }
        val job = Job.getInstance(conf, "Word Count")
        if (inputPath.startsWith("hdfs")) {
        } else if (inputPath.startsWith("/cs441/")) {
            conf.set("fs.defaultFS", "hdfs://localhost:9000") // Adjust this with your HDFS host if using HDFS
        } else {
            conf.set("fs.defaultFS", "file:///")
        }
        job.setJarByClass(getClass)
        job.setMapperClass(classOf[MapTokenizer]) // Set your mapper class here
        job.setReducerClass(classOf[ReduceTokenizer])

        job.setMapOutputKeyClass(classOf[Text])
        job.setMapOutputValueClass(classOf[IntWritable])

        job.setOutputKeyClass(classOf[Text])
        job.setOutputValueClass(classOf[IntWritable])

        job.setInputFormatClass(classOf[TextInputFormat])
        job.setOutputFormatClass(classOf[TextOutputFormat[Text, IntWritable]])

        FileInputFormat.addInputPath(job, new Path(inputPath))
        FileOutputFormat.setOutputPath(job, new Path(outputPath))

        job.waitForCompletion(true)
    }
}