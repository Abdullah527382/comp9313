import org.apache.spark.graphx._
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import collection.mutable.HashSet
import collection.mutable._


object Problem2 {
  def main(args: Array[String]) = {
    val conf = new SparkConf().setAppName("COUNTTRI").setMaster("local")
    val sc = new SparkContext(conf)
    
    val inputFile = args(0)
    val outputFolder = args(1)
    val edges = sc.textFile(inputFile )


    val edgelist = edges.map(x => x.split(" ")).map(x=> Edge(x(1).toLong, x(2).toLong, true)) 
    val graph = Graph.fromEdges[Double, Boolean](edgelist, 0.0)

    val initialGraph = graph.mapVertices((id, _) => HashSet[List[VertexId]]())

    val res = initialGraph.pregel(HashSet[List[VertexId]](), 3)(
      (id, ns, newns) => newns, // Vertex Program
      triplet => {  // Send Message
        var mes = new HashSet[List[VertexId]]()
        for(a <- triplet.srcAttr){
          if(!a.contains(triplet.srcId))
            mes += a :+ triplet.srcId
        }
        if(triplet.srcAttr.isEmpty){
          var x = List(triplet.srcId) 
          mes += x
        }
        //println(mes)
        Iterator((triplet.dstId, mes))
      },
      (a, b) => a++b // Merge Message
    )
    res.vertices.collect()     

    val tmp_res = res.vertices.map{case(id, attr) => {
        if (attr.size > 0){
        val attrList = attr.toList.filter(x => x(0)==id)
        val sotredList = attrList.sortWith((a, b) => {
            if (a(1) > b(1)) true
                 else if (a(1) == b(1)) {
                    if (a(2) > b(2)) true
                    else false
                 }
                 else false
          })
        (id, sotredList)
        }
        else (id, attr)
      }
    }

    val final_res =tmp_res.sortBy{case(id, attr)=>id}.map{case(id, attr) => {
        var i =0
        var tmp = ""
        for(a <- attr){
          if(a(0) == id)
            tmp += a.mkString("->")+";"
        }
        (id+":"+tmp)
      }
    }

    final_res.saveAsTextFile(outputFolder)
  }
}
