package Tokenizer

import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.{Encoding, EncodingRegistry, EncodingType, IntArrayList}
import scala.util.matching.Regex
import scala.io.Source
import UtilitiesDir.Utilities.*
import java.io.{InputStream, PrintWriter}

object JTokkitTokenizer {
    private val registry: EncodingRegistry = Encodings.newLazyEncodingRegistry
    private val encoding: Encoding = registry.getEncoding(EncodingType.CL100K_BASE)

    def tokenize(text: String): Array[Int] = {
        val tokens: IntArrayList = encoding.encode(text)
        (0 until tokens.size()).map(tokens.get).toArray
    }
}
