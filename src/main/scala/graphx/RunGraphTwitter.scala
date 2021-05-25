package graphx

import graphx.graphanalysis.GraphBuilder
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.graphx.{Graph, GraphLoader, VertexId}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{SQLContext, SparkSession}

import scala.util.hashing.MurmurHash3

object RunGraphTwitter {

  def main(args: Array[String]): Unit = {
    // Set log file to only see print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    // Spark entry point
    val spark = SparkSession.builder().master("local[2]").config("spark.sql.warehouse.dir", "file:///Users/User/IdeaProject/graphx-experiment/").getOrCreate().sparkContext
    //simpleGraphOperations(spark)
    graphLoaderExamples(spark)
    //tryAgainGraph(spark)

  }

  private def simpleGraphOperations(spark: SparkContext) = {

    // read data from csv file
    val sc = spark
    val sqlContext = new SQLContext(sc)
    val file = sc.textFile("src/main/scala/graphx/twitter.txt")

    val df = sqlContext.read
    //df.show()
    //df.printSchema()
      .format("com.databricks.spark.csv")
      .option("header", "false")  // Use first line of all files as header
      .option("inferSchema", "true")  // Automatically infer data types
      .load("src/main/scala/graphx/twitter.txt")

    println("successfully read the file")
    println("file is "+df.show())

    // create edge RDD of type RDD[(VertexId, VertexId)]
    val edgesRDD: RDD[(VertexId, VertexId)] = file.map(line => line.split(" "))
      .map(line =>
        (MurmurHash3.stringHash(line(0).toString), MurmurHash3.stringHash(line(1).toString)))

    // Create a graph
    val graph: Graph[Int, Int] = Graph.fromEdgeTuples(edgesRDD, 1)

    // you can see your graph
    graph.triplets.collect.foreach(println(_))

    // select data and write to new file
    //val selectedData = df.select("src")
    //println("file is "+selectedData.show())
    //selectedData.write
    //  .format("com.databricks.spark.csv")
    //  .option("header", "true")
    //  .option("inferSchema", "true")  // Automatically infer data types
    //  .save("src/main/scala/com/spark/graphx/twitter/newtwitter.csv")

    println("successfully process the file")



    // perform simple graph operations on the data
  }

  private def graphLoaderExamples(spark: SparkContext) ={


    // Load my user data and parse into tuples of user id and attribute list
//    val users = (spark.textFile("src/main/scala/graphx/users.txt")
//      .map(line => line.split("\t")).map(parts => (parts.head.toLong, parts.tail)))

    val users = spark.textFile("src/main/scala/graphx/users.txt").map{l => val lineSplits = l.split("\\s+")
      val id = lineSplits(0).trim.toLong
      val data = lineSplits.slice(1,lineSplits.length).mkString(" ")
      (id,data)}

    // Parse the edge data which is already in userId -> userId format
    val relationships = GraphLoader.edgeListFile(spark, "src/main/scala/graphx/twitter.txt")

    // Attach the user attributes
    val graph = relationships.outerJoinVertices(users){
      case(uid, name, Some(attrList)) => attrList
      // Some users may not have attributes so we set them as empty
      case(uid, name, None) => "Missing data"
    }
    //graph.triplets.foreach(println(_))

    // Restrict the graph to users with usernames and names
    //val subgraph = graph.subgraph(vpred = (vid, attr) => attr.size == 2)

    // Compute the PageRank
    val pagerankGraph = graph.pageRank(0.001)
    val ranks = graph.pageRank(0.0001).vertices

    //pagerankGraph.triplets.foreach(println(_))
    // Get the attributes of the top pagerank users
    val userInfoWithPageRank = graph.outerJoinVertices(pagerankGraph.vertices) {
      case (uid, attrList, Some(pr)) => (pr, attrList)
      case (uid, attrList, None) => (0.0, attrList)
    }
    //userInfoWithPageRank.triplets.foreach(println(_))
    //println(userInfoWithPageRank.vertices.top(20)(Ordering.by(_._2._1)).mkString("\n"))

    /**
      * ranking
      * importance of each vertex in a graph
      */
    val ranksByUsername = users.join(ranks).map {
      case (id, (username, rank)) => (username, rank)
    }
    // Print the result
    println(ranksByUsername.collect().mkString("\n"))
  }

  private def tryAgainGraph(spark: SparkContext) = {

    val filepath: String = "src/main/scala/graphx/twitter.txt"
    val userName = "src/main/scala/graphx/users.txt"

    val graph = GraphBuilder.loadFromFile(spark, filepath)

    //triplets output format : ((vId_src,src_att),(vId_dst,dst_att),property)
    //graph.triplets.foreach(println(_))

    val outerDegree = graph.outDegrees

    val degreeGraph = graph.outerJoinVertices(outerDegree){
      case(uid, deg, Some(attrList)) => attrList
      case(uid, deg, None) => "Missing data"
    }
    println("----- THIS IS DEGREE GRAPH -----")
    //degreeGraph.triplets.foreach(println(_))
    //degreeGraph.vertices.foreach(println(_))
    //degreeGraph.edges.foreach(println(_))

    val verts = spark.textFile(userName).map{l => val lineSplits = l.split("\\s+")
      val id = lineSplits(0).trim.toLong
      val data = lineSplits.slice(1,lineSplits.length).mkString(" ")
      (id,data)}

    val nameGraph = graph.outerJoinVertices(verts) {
      case(uid, deg, Some(attrList)) => attrList
      case(uid, deg, None) => "Missing name"
    }

    println("----- THIS IS NAME GRAPH -----")
    nameGraph.triplets.foreach(println(_))
    // ARGHHHHHHHHHH
//    graph.triplets.map(
//      triplet => triplet.srcAttr + " follows " + triplet.dstAttr
//    ).collect.foreach(println(_))
//
//    val ccGraph = graph.connectedComponents()
//    ccGraph.triplets.map(
//      triplet => triplet.srcAttr + " follows " + triplet.dstAttr
//    ).collect.foreach(println(_))

  }

}
