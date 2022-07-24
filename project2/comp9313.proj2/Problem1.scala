/* SimpleApp.scala */
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import scala.collection.mutable.ArrayBuffer
import org.apache.spark.rdd.RDD;
import org.apache.spark.broadcast.Broadcast
import scala.collection.immutable.ListMap

object Problem1 {
  def main(args: Array[String]): Unit = {

    // Some file in the hdfs fs, replace these as arg variables
    val K = args(0) // e.g. 3
    val stopWordsFileAddr = args(1) // e.g. "project2/stopwords.txt"
    val sampleFileAddr = args(2) // e.g. "project2/tiny-doc.txt"
    val outputFolder = args(3) // e.g. "project2/output"
    // Create a new spark configuration and create a context out of that
    val conf = new SparkConf().setAppName("Problem1").setMaster("local")
    val sc = new SparkContext(conf)
    var sampleData = sc
      .textFile(sampleFileAddr)
    // Read the file into values, make sure all lower case and split on special chars
    val stopWords =
      sc.textFile(stopWordsFileAddr)
        .flatMap(line => line.split(" "))
    val stopWordsBroadcast = sc.broadcast(stopWords.collect())
    var sampleBroadcast = sc.broadcast(sampleData.collect())

    var filteredData: Array[String] = Array[String]()
    stopWordsBroadcast.value.foreach(word => {
      filteredData = filterStopWords(sampleBroadcast, filteredData, word)
    })

    // Remove all the dates from the file
    filteredData = removeDate(filteredData)

    // Convert our filtered data into a list, separate by identifier: space
    val filteredDataList = filteredData.map(_.split(" ").toList)

    // Count all the co-occurring frequencies and store into new value
    val countCooFreq = filteredDataList
      // Get all the combinations (by 2) of each value in list
      .flatMap(_.combinations(2))
      // Map the value to count of 1
      .map((_, 1))
      // Group by the word pair
      .groupBy(_._1)
      // Map the key and value (list) by adding the counts together
      .map { case (key, list) => key -> list.map(_._2).reduce(_ + _) }

    // Sort first by the values count then alphabetically in decreasing order, and only take the first K
    // -_._2, _._1(0)
    val sorted =
      countCooFreq.toArray
        .sortBy { case (k, v) => (-v, k(0)) }
        .take(K.toInt)
    // var sorted =
    //   res.toSeq.sortBy(_._1(0))

    // Map the output to be of desired format, into an RDD so we can output to dest folder
    val outputRDD = sc.parallelize(sorted.map { case (key, value) =>
      key(0) + "," + key(1) + "\t" + value
    })
    outputRDD.saveAsTextFile(outputFolder)
  }

  // Our helper functions which just clean the data
  // Remove the stop words
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
          // Ignore words that aren't from lineWord => a, lineword <= z
          if (lineWord.charAt(0) <= 'z' && lineWord.charAt(0) >= 'a') {
            concatString += " " + lineWord
          }
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

  // Remove the date from our Array[String] since it seems pointless
  def removeDate(
      filteredData: Array[String]
  ): Array[String] = {
    return filteredData.map(line => {
      var lineArray = line.split(",");
      lineArray(1)
    })
  }

}
