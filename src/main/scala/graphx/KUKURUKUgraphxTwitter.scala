package graphx

import org.apache.spark.SparkContext
import org.apache.spark.graphx.{Edge, Graph}
import org.apache.spark.sql.SparkSession

// REFERENCE : https://kukuruku.co/post/social-network-analysis-spark-graphx/
object KUKURUKUgraphxTwitter {

  def main(args: Array[String]) = {
    val spark = SparkSession.builder().master("local[2]").getOrCreate().sparkContext
  }

  private def buildAndComputeGraphx(spark: SparkContext)= {
    // define the path of data, say in HDFS
    val edgesPath = "src/main/scala/graphx/twitter.txt"
    val verticesPath = "src/main/scala/graphx/users.txt"

    // read the file
    val userDetails = spark.textFile(verticesPath)
    val edgeGraph = spark.textFile(edgesPath)

    // process the data using the .map() transformation to separate
    val vertices = userDetails.map{ line =>
      val fields = line.split("\t")
      (fields(0).toLong, fields(1))
    }

    // construct the edge using the Edge constructor
    val edges = edgeGraph.map{ line =>
      val fields = line.split(" ")
      Edge(fields(0).toLong, fields(1).toLong, 0)
    }

    // Define a default user in case there are relationship with missing user
    val defaultUser = ("John", "Missing")

    // get a list of vertices and edges that can be passed to the Graph constructor
    //val graph = Graph(vertices, edges, defaultUser).cache()
    //graph.triplets.foreach(println(_))
  }
}
