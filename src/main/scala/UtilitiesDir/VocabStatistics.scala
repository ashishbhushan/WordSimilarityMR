package UtilitiesDir

import UtilitiesDir.FileWrite.*
import org.apache.hadoop.fs.{FileSystem, Path}

object VocabStatistics {

    private def vocabPreprocess(text: String): String = {
        text.replace("]","],").replaceAll("\\s+", "")
    }

    def getYamlFile(text: String, outputDir: String, fs: FileSystem): Unit = {
        val flag = false
        val textSplit = text.split("\n")
        val processedLines = textSplit.map(vocabPreprocess)

        val vocabPath = new Path(s"$outputDir/vocabulary/vocabulary.yaml")
        writeToFile(processedLines, vocabPath, fs, flag)
    }
}
