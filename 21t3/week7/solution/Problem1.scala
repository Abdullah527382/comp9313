package comp9313.proj2
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import scala.collection.mutable.ArrayBuffer

object Problem1 {
  
  def pairGen(wordArray: Array[String]) : ArrayBuffer[(String, Int)] = {
    val abuf = new ArrayBuffer[(String, Int)]
    
    for(i <- 0 to wordArray.length -1){
      val term1 = wordArray(i)
      if(term1.length()>0 && term1.charAt(0) <= 'z' && term1.charAt(0) >= 'a'){
        for(j <- i+1 to wordArray.length - 1){
          val term2 = wordArray(j)
          if(term2.length()>0 && term2.charAt(0) <= 'z' && term2.charAt(0) >= 'a'){
            abuf.+=:(term1 + "," + term2, 1)
          }
        }
      }
    }    
    return abuf
  }
  
  def pairComp(term1: String, arr: Array[(String,Int)]) : 
    ArrayBuffer[(String, String, Double)] = {
    val cbuf = new ArrayBuffer[(String, String, Double)]
    var sum = 0d   
    
    for(i <- 0 to arr.length -1){
      val pair = arr(i)
      sum += pair._2
    }    
    
    for(i <- 0 to arr.length -1){
      val pair = arr(i)
      val freq = pair._2/sum
      cbuf.+=:(term1, pair._1, freq)
    }     
   
    return cbuf
  }
  
  def main(args: Array[String]) {
    val inputFile = args(0)
    val outputFolder = args(1)
    val conf = new SparkConf().setAppName("RelFreq").setMaster("local")
    val sc = new SparkContext(conf)

    val input =  sc.textFile(inputFile).map(_.toLowerCase()).map(_.split("[\\s*$&#/\"'\\,.:;?!\\[\\](){}<>~\\-_]+"))
    val pairs = input.flatMap(line => pairGen(line)).reduceByKey(_+_)    
    val pairs2 = pairs.map{case(x, y) => (x.split(",")(0), (x.split(",")(1), y))}.groupByKey()
        
    val pairs3 = pairs2.flatMap{case (x, y) => pairComp(x, y.toArray)}
    val res = pairs3.sortBy(_._2).sortBy(_._3, false).sortBy(_._1).map{case(x, y, z) => x + " " + y+ " " + z}
    
    res.saveAsTextFile(outputFolder)            
  }
}