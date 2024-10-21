package UtilitiesDir

import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.{Encoding, EncodingRegistry, EncodingType, IntArrayList}

object JTokkitTokenizer {
    private val registry: EncodingRegistry = Encodings.newLazyEncodingRegistry
    private val encoding: Encoding = registry.getEncoding(EncodingType.CL100K_BASE)

    def tokenize(text: String): Array[Int] = {
        text.split(" ").flatMap(word => {
            val tokens: IntArrayList = encoding.encode(word)
            (0 until tokens.size()).map(tokens.get).toArray
        })
    }
}
