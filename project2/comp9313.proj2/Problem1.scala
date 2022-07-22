/* SimpleApp.scala */
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

object Problem1 {
  def main(args: Array[String]): Unit = {

    // Some file in the hdfs fs
    val sampleFile = "project2/tiny-doc.txt"
    val stopWordsFile = "project2/stopwords.txt"

    // Create a new spark configuration and create a context out of that
    val conf = new SparkConf().setAppName("Problem1").setMaster("local")
    val sc = new SparkContext(conf)

    // Read the file into values
    val stopWords = sc.textFile(stopWordsFile)
    val sampleData = sc.textFile(sampleFile)

    // Store the stop words into an array
    val stopWordsArray = stopWords.flatMap(_.split(" "))
    stopWordsArray.foreach(word => println(word))
  }

  // def removeStopWords () :

}
