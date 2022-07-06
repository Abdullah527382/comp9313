/* SimpleApp.scala */
import org.apache.spark.sql.SparkSession

object Problem1 {
  def main(args: Array[String]): Unit = {

    // Some file in the hdfs fs
    val sampleFile = "spark-test/tiny-doc.txt"
    val stopWordsFile = "spark-test/stopwords.txt"

    // Initialize a spark session
    val spark = SparkSession.builder.appName("Problem 1").getOrCreate()

    // Read both files into a value array
    val stopWords = spark.read.textFile(stopWordsFile).cache()
    val sampleData = spark.read.textFile(sampleFile).cache()

    // Filter out the sampleData from any stopwords e.g. to

    val sampleData = sampleData.filter(line => {
      line.contains(stopWords.filter(word => {
        word.c
      }))
    })
    val numBs = logData.filter(line => line.contains("b"))
    println(s"Lines with a: $numAs, Lines with b: $numBs")
    spark.stop()
  }
}
