import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, udf, when, size, typedLit}
import scala.collection.mutable.Map
import scala.collection.mutable.Set
import scala.collection.mutable.Buffer
import scala.collection.mutable
import org.apache.spark.rdd.RDD
import util.control.Breaks._

// Run like this: 
// spark-submit --class "SimilarNews" target/scala-2.12/similar-news_2.12-1.0.jar project2/in01.txt out01 0.4
// Takes args: input file, output dir and tao t 
object SimilarNews {
    def main(args: Array[String]): Unit = {
    val t1 = System.nanoTime 

    // Get the arguments
    val inputFile = args(0) // e.g. "project2/tinydoc.txt"
    val outputDir = args(1) // e.g. "project2/out1"
    val t = args(2).toFloat // e.g. 0.5

    // val conf = new SparkConf().setAppName("Similar News").setMaster("local")
    // val context = new SparkContext(conf)

    val sc = SparkSession.builder().appName("Similar News").master("local").getOrCreate()
    import sc.implicits._

    var sampleData = sc.read.textFile(inputFile).rdd.zipWithIndex.map{ case (element, index) => 
            (index, 
             if (element.split(",").length > 1) {
                val wordArray = element.split(",")(1).split(" ")
                // Define empty list
                var savedList: Map[String, Int] = Map()
                // Tokenize using a hashmap by keeping count of the frequency per line
                wordArray.map(word => {
                    if (!savedList.contains(word)){
                        savedList += (word->1)
                    } else {
                        savedList(word) = savedList(word) + 1
                    }
                    word + savedList(word)
                }).toList.sortWith(_ < _)
            } else {
                List.empty[String]
            },
            element.split(",")(0).substring(0,4))
    }

    // Create a sorted dataframe list of word counts
    // TO-DO: Account for null headlines as well

    val counts = sc.sparkContext.broadcast(
        sampleData
        .flatMap(line => line._2)
        .map( (s:String) => (s,1) )
        .reduceByKey( _ + _ )
        .sortBy[Int]( (pair:Tuple2[String,Int]) => pair._2 )
        .collectAsMap())

    // Sort all the words within a headline respective to their occurrence/freq
    val keyPairs = sampleData.map(record =>
        (record._1, 
        // Change each word into its frequency sorted by ascending order
        record._2.sortWith(counts.value(_).toInt < counts.value(_).toInt)
        // Ignore any blank headlines
        ,record._3
        )
    )

    keyPairs.collect()
    // Create an inverted index for all terms and their headlines
    // http://cowa.github.io/2015/12/07/inverted-index-scala/
    val invertedIndex = keyPairs
        .map(headline => (headline._1, headline._2))
        .flatMap(x => x._2.map(y => (y, x._1)))
        .groupBy(_._1).map(p => (p._1, p._2.map(_._2).toList)).collect().toMap
        // .map(p => (p(0), p(1).map(_._2).toVector))


    // Generate our candidate pairs from records (index, [tokens], year)

    List(keyPairs.collect().toSeq.sortBy(_._2.length): _*).foreach(println)

    var cp: Set[(Int, Int)] = candidatePairs(keyPairs, t);
    var records = keyPairs.map(record => record._2).collect().toList
    var sortedcp = cp.toSeq.sortBy{case (k,v) => (k, v)}.map(
        pair => ((pair._1, pair._2), jaccard(records(pair._1), records(pair._2))));

    sortedcp.foreach(println)

    val outputRDD = sc.sparkContext.parallelize(sortedcp.map { case (key, value) =>
      key._1 + "," + key._2 + "\t" + value
    })
    outputRDD.saveAsTextFile(outputDir)
    }

    def prefixLength (s: List[String], t: Float): Integer = {
        return ((s.length * t).ceil + 1).toInt
    }

    def overlapConstraint (len_s1: Integer, len_s2: Integer, t: Float): Integer = {
        return ((t / (1.0 + t)) * (len_s1 + len_s2)).ceil.toInt
    }

    def jaccard(a: List[String], b: List[String]): Double = {
        // (Set1.toList.intersect(Set2.toList)).size.toDouble / (Set1.toList.union(Set2.toList)).distinct.size.toDouble
        return ((a.intersect(b).size.toDouble / a.union(b).size.toDouble))
    }

    def candidatePairs (
            keyPairs: RDD[(Long, List[String], String)], 
            t: Float): Set[(Int, Int)] = {
        var cp: Set[(Int, Int)] = Set.empty[(Int, Int)]

        // List(keyPairs.collect().toSeq.sortBy(_._2.length): _*).foreach(record => 
        for (record <- List(keyPairs.collect().toSeq.sortBy(_._2.length): _*))
        {

            var records = keyPairs.map(record => record._2).collect().toList
            var yearByIndexes = keyPairs.map(record => (record._1, record._3)).collect().toMap
            // Access the element like
            var ii: Map[String, Set[(Long, Int)]] = records.flatten
                .foldLeft(Map.empty[String, Set[(Long, Int)]]){
                (map, elem) => 
                    map += elem -> Set.empty[(Long, Int)]
                }
            // FIXME: You need to test the last record, it is being ignored currently as per output
            
            var recordxIndex = record._1.toInt
            var recordx = record._2
            // Candiate pairs
            // Prefix length of a given headline
            var recordpx = prefixLength(recordx, t)
            // recordp = recordp.min(record._2.length)
            var overlapByY: Map[Int, Int] = (0 until records.length)
                .foldLeft(Map.empty[Int, Int]) { (map, elem) =>
                map += elem -> 0
            }
            var i = 0;
            for (i <- 0 until recordpx){
                var recordxElement = recordx(i)
                breakable {
                    for ((recordyIndex,j) <- ii(recordxElement)){
                        var recordyIndexInt = recordyIndex.toInt
                        var recordy = records(recordyIndexInt)
                        if (recordy.length < t * recordx.length){
                            break;
                        }
                        var alpha = overlapConstraint(recordx.length, recordy.length, t)
                        var upperBound = 1 + (recordx.length).min(recordy.length - j)

                        if (overlapByY(recordyIndexInt) + upperBound >= alpha){
                            // A[Y] = A[Y] + 1
                            overlapByY(recordyIndexInt) = (overlapByY(recordyIndexInt) + 1)
                        } else {
                            overlapByY(recordyIndexInt) = (0);
                        }
                    }
                    
                }
                ii(recordxElement) += ((recordxIndex, i))
            }

            for ((recordyIndex, overlapVal) <- overlapByY){
                //println("OVERLAP:  " + recordyIndex)
                var overlap = overlapVal
                var recordy = records(recordyIndex)
                var recordpy = prefixLength(recordy, t)
                var wx = recordx(recordpx - 1)
                var wy = recordy(recordpy - 1)
                var alpha = overlapConstraint(recordx.length, recordy.length, t)
                if (wx < wy){
                    var unbound = overlap + recordx.length - recordpx
                    if (unbound >= alpha){
                        // O ← O + ˛ ˛x[(px + 1) . . |x| ]INTERSECTT y[(A[y] + 1) . . |y|] ˛ ˛
                        overlap += (recordx.slice(recordpx, recordx.length))
                            .intersect((recordy.slice(overlap + 1, recordy.length))).length
                    }
                } else {
                    var unbound = overlap + recordy.length - recordpy
                    if (unbound >= alpha){
                        // O ← O + ˛ ˛x[(A[y] + 1) . . |x|] INTERSECT y[(py + 1) . . |y|]
                        overlap += (recordx.slice(overlap, recordy.length))
                            .intersect((recordy.slice(recordpy + 1, recordy.length))).length
                    }
                }
                if (overlap >= alpha
                    && recordxIndex < recordyIndex
                    && yearByIndexes(recordyIndex) != yearByIndexes(recordxIndex)){
                    cp += (recordxIndex->recordyIndex)
                }

            }
            // cp.foreach(println)
        }
    
    return cp;
    }
}
