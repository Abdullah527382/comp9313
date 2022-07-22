/* SimpleApp.scala */
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import scala.collection.mutable.ArrayBuffer
import org.apache.spark.rdd.RDD;
import org.apache.spark.broadcast.Broadcast

object Problem1 {
  def main(args: Array[String]): Unit = {

    // Some file in the hdfs fs
    val sampleFileAddr = "project2/tiny-doc.txt"
    val stopWordsFileAddr = "project2/stopwords.txt"
    val outputFolder = "project2/output"
    // Create a new spark configuration and create a context out of that
    val conf = new SparkConf().setAppName("Problem1").setMaster("local")
    val sc = new SparkContext(conf)
    var sampleData = sc
      .textFile(sampleFileAddr)
    // Read the file into values
    val stopWords =
      sc.textFile(stopWordsFileAddr).flatMap(line => line.split(" "))
    val stopWordsBroadcast = sc.broadcast(stopWords.collect())
    var sampleBroadcast = sc.broadcast(sampleData.collect())

    var filteredData: Array[String] = Array[String]()
    stopWordsBroadcast.value.foreach(word => {
      filteredData = filterStopWords(sampleBroadcast, filteredData, word)
    })

    filteredData.foreach(word => {
      println(word)
    })
  }

  def filterStopWords(
      sampleBroadcast: Broadcast[Array[String]],
      filteredData: Array[String],
      word: String
  ): Array[String] = {
    if (filteredData.isEmpty) {
      return sampleBroadcast.value.map(line => {
        var lineArray = line.split("[, ]").filter(_ != word);
        var concatString: String = lineArray(0) + ","
        for (i <- 1 to lineArray.length - 1) {
          val lineWord = lineArray(i)
          concatString += " " + lineWord
        }
        concatString
      })
    } else {
      return filteredData.map(line => {
        var lineArray = line.split(" ").filter(_ != word);
        var concatString: String = lineArray(0)
        for (i <- 1 to lineArray.length - 1) {
          val lineWord = lineArray(i)
          concatString += " " + lineWord
        }
        concatString.replace(", ", ",")
      })
    }
  }
}
