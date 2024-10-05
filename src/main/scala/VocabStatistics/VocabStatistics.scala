package VocabStatistics

import DataProcessor.FileWrite.*
import org.apache.hadoop.fs.{FileSystem, Path}
import scala.io.Source
import scala.util.matching.Regex

object VocabStatistics {

    private def vocabPreprocess(text: String): String = {
        text.replace("]","],").replaceAll("\\s+", "")
    }

    def getYamlFile(text: String, outputDir: String, fs: FileSystem): Unit = {
        val flag = false
        val textSplit = text.split("\n")
        val processedLines = textSplit.map(vocabPreprocess)

        val vocabPath = new Path(s"$outputDir/vocabulary.yaml")
        writeToFile(processedLines, vocabPath, fs, flag)

        val inputStream = fs.open(vocabPath)
        val yamlContents = Source.fromInputStream(inputStream).getLines().mkString("\n")
        val bracketPattern: Regex = """\[(.*?)]""".r

        // Process each line, extract tokens from brackets
        val extractedTokens = yamlContents.split("\n").map { line =>
            bracketPattern.findFirstMatchIn(line) match {
                case Some(matched) => matched.group(1) // Extract the token inside the brackets
                case None => "" // In case no match is found (shouldn't happen here)
            }
        }

        val tokensPath = new Path(s"$outputDir/tokens.txt")
        writeToFile(extractedTokens, tokensPath, fs, flag)
    }
}
