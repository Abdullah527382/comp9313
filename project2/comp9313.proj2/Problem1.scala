/* SimpleApp.scala */
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import scala.collection.mutable.ArrayBuffer
import org.apache.spark.rdd.RDD;
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
      if (filteredData.isEmpty) {
        filteredData = sampleBroadcast.value.map(line => {
          var lineArray = line.split("[, ]").filter(_ != word);
          var concatString: String = lineArray(0) + ","
          for (i <- 1 to lineArray.length - 1) {
            val lineWord = lineArray(i)
            concatString += " " + lineWord
          }
          concatString
        })
      } else {
        filteredData = filteredData.map(line => {
          var lineArray = line.split(" ").filter(_ != word);
          var concatString: String = lineArray(0)
          for (i <- 1 to lineArray.length - 1) {
            val lineWord = lineArray(i)
            concatString += " " + lineWord
          }
          concatString.replace(", ", ",")
        })
      }
    })

    filteredData.foreach(word => {
      println(word)
    })
    // // val filteredData = sampleData.map(line => line.replace("to", "REMOVED"))
    // filteredData.foreach(line => println(line))
    // sampleData.flatMap(_.split("[, ]")).foreach(line => println(line))

  }

  // Given an array of strings, remove a stopword
  def removeStopWord(
      words: String,
      stopWords: RDD[String]
  ): ArrayBuffer[String] = {
    val cleanLine = new ArrayBuffer[String]

    // Break the line into an array of words:
    val wordArray = words.split(" ")
    // Loop through the stop words
    stopWords.foreach(stopWord => {
      for (i <- 0 to wordArray.length - 1) {
        val lineWord = wordArray(i)
        if (lineWord != stopWord) {
          cleanLine.+=:(lineWord)
        }
      }
    })
    println(cleanLine)
    return cleanLine
  }

}
