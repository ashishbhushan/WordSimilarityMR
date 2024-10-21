package UtilitiesDir

import org.deeplearning4j.models.word2vec.Word2Vec

object EmbeddingGenerator {

  def generateEmbeddings(word2Vec: Word2Vec, sentences: List[List[Int]]): List[(Int, Array[Double])] = {
    val embeddings: List[(Int, Array[Double])] = sentences.flatMap { sentence =>
      sentence.flatMap { tokenId =>
        Option(word2Vec.getWordVector(tokenId.toString)).map(embedding => (tokenId, embedding))
      }
    }
    embeddings
//    val uniqueEmbeddings: Map[Int, Array[Double]] = embeddings
//      .groupBy(_._1) // Group by token ID
//      .map { case (tokenId, embeddings) =>
//        val firstEmbedding = embeddings.head // Or use a strategy to combine embeddings
//        (tokenId, firstEmbedding._2) // Take the first embedding
//      }
//    uniqueEmbeddings.foreach { case (tokenId, embedding) =>
//      val embeddingStr = embedding.mkString(", ")
//      println(s"$tokenId $embeddingStr")
//    }
  }
}
