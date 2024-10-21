package UtilitiesDir

import UtilitiesDir.DataPreprocessor.processData

object DataSharder {
    def shardData(data: String): String = {
        val words = processData(data).split("\\s+")
        val shardSize = words.length
        words.grouped(shardSize).map(_.mkString(" ")).toArray.mkString
    }
}