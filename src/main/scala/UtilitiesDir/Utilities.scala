package UtilitiesDir
import com.typesafe.config.{Config, ConfigFactory}

object Utilities {
    private val applicationConfig: Config = ConfigFactory.load()
    val testInputFile: String = applicationConfig.getString("app.testInputFile")
    val wikitext_test: String = applicationConfig.getString("app.wikitext_test")
    val wikitext_validate: String = applicationConfig.getString("app.wikitext_validate")
    val tokensText: String = applicationConfig.getString("app.tokensText")
    val shardedText: String = applicationConfig.getString("app.shardedText")
    val mrtOutput: String = applicationConfig.getString("app.mrtOutput")
    val vocabYaml: String = applicationConfig.getString("app.vocabYaml")
    val mapRedTokenOut: String = applicationConfig.getString("app.mapRedTokenOut")
    val linesPerShard = 20
}
