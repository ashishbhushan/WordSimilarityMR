package DataProcessor

import scala.util.matching.Regex

object DataPreprocessor {
    def processData(text: String): String = {
        val pattern: Regex = "\\w+".r
        val result = pattern.findAllIn(text.replace("_","").toLowerCase).toList
        result.mkString(" ")
    }
}