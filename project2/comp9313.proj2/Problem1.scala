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
    // Read the file into values
    val stopWords =
      sc.textFile(stopWordsFileAddr).flatMap(line => line.split(" "))
    val stopWordsBroadcast = sc.broadcast(stopWords.collect())
    var sampleBroadcast = sc.broadcast(sampleData.collect())

    var filteredData: Array[String] = Array[String]()
    stopWordsBroadcast.value.foreach(word => {
      filteredData = filterStopWords(sampleBroadcast, filteredData, word)
    })

    filteredData = removeDate(filteredData)
    val tokens = filteredData.map(_.split(" ").toList)

    val coo = tokens
      .flatMap(_.combinations(2))
      .map((_, 1))
      .groupBy(_._1)
      .map { case (key, list) => key -> list.map(_._2).reduce(_ + _) }
    //   .toSeq
    //   .sortWith((list: (List[String], Int)) => list._1(0) > list._1(1))
    // // coo.foreach(println)

    // Sort alphhabetically
    val res = ListMap(coo.toSeq.sortBy(-_._2): _*).take(3)
    // case class record(list: List[String], count: Int)
    var sorted =
      res.toSeq.sortBy(_._1(0))

    // res.foreach(println)
    sorted.map { case (key, value) =>
      key(0) + "," + key(1) + "\t" + value
    }
    val outputRDD = sc.parallelize(sorted.map { case (key, value) =>
      key(0) + "," + key(1) + "\t" + value
    })
    outputRDD.saveAsTextFile(outputFolder)
    // sorted.saveAsTextFile(outputFolder)

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

  // Remove the date from our Array[String] since it seems pointless
  def removeDate(
      filteredData: Array[String]
  ): Array[String] = {
    return filteredData.map(line => {
      var lineArray = line.split(",");
      lineArray(1)
    })
  }

  //

}
