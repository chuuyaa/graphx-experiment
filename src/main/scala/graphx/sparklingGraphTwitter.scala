package graphx

import java.io.{BufferedWriter, File, FileWriter, PrintWriter}

import ml.sparkling.graph.api.operators.algorithms.community.CommunityDetection.ComponentID
import ml.sparkling.graph.api.operators.measures.VertexMeasureConfiguration
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkContext
import org.apache.spark.graphx.{Graph, GraphLoader}
import org.apache.spark.sql.SparkSession
import graphx.indices.OperatorsNew._

import ml.sparkling.graph.operators.measures.graph.Modularity

import scala.reflect.ClassTag


object sparklingGraphTwitter extends Serializable {


  def main(args: Array[String]): Unit = {
    // Set log file to only see print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    // Spark entry point
    val spark = SparkSession.builder().master("local[2]").config("spark.sql.warehouse.dir", "file:///Users/User/IdeaProject/graphx-experiment/").getOrCreate().sparkContext

    val sqlContext = new org.apache.spark.sql.SQLContext(spark)
    import sqlContext.implicits._

    graphxProcessVOne(spark)

  }

  private def graphxProcessVOne(spark: SparkContext)={

    val sqlContext= new org.apache.spark.sql.SQLContext(spark)
    import sqlContext.implicits._
    /**
      * Load my user data and parse into tuples of user id and attribute list
      */
    val users = spark.textFile("src/main/scala/graphx/users.txt").map{l => val lineSplits = l.split("\\s+")
      val id = lineSplits(0).trim.toLong
      val data = lineSplits.slice(1,lineSplits.length).mkString(" ")
      (id,data)}

    /**
      * Parse the edge data which is already in userId -> userId format
       */
    val relationships = GraphLoader.edgeListFile(spark, "src/main/scala/graphx/twitter-small.txt")

    /**
      * Attach the user attributes
       */
    val graph = relationships.outerJoinVertices(users){
      case(uid, name, Some(attrList)) => attrList
      // Some users may not have attributes so we set them as empty
      case(uid, name, None) => "Missing data"
    }
    //val fw = new BufferedWriter(new FileWriter("testOut.txt"))
//    val fw = new FileWriter("testOUTPUT.txt", true)
//    try {

//    graph.triplets.foreach(
//      value => {
//        fw.write(value+"")
//      }
//    )
//    fw.close()

    println("GRAPH TRIPLETS :")
    graph.triplets.foreach(println(_))

    //try saved the output to a JSON file
    val tripletsDataFrame = graph.triplets.map(triplet => (triplet.srcId, triplet.srcAttr, triplet.dstId, triplet.dstAttr, triplet.attr))
      .toDF("srcId", "srcAttr", "dstId", "dstAttr", "edgeAttr")
    tripletsDataFrame.write.json("graphOutputJSON.txt")

    //graph.triplets.saveAsTextFile("testOutV.txt")

    //fw.write("GRAPH TRIPLETS :")
      //fw.write(result)
    /**
      * Compute the PageRank
      */
    val pagerankGraph = graph.pageRank(0.001)
    val ranks = graph.pageRank(0.0001).vertices

//    println("PAGERANK GRAPH TRIPLETS :")
//    pagerankGraph.triplets.foreach(println(_))

    /**
      * Get the attributes of the top pagerank users
       */
    val userInfoWithPageRank = graph.outerJoinVertices(pagerankGraph.vertices) {
      case (uid, attrList, Some(pr)) => (pr, attrList)
      case (uid, attrList, None) => (0.0, attrList)
    }
    //userInfoWithPageRank.triplets.foreach(println(_))
    //println(userInfoWithPageRank.vertices.top(20)(Ordering.by(_._2._1)).mkString("\n"))

    /**
      * INDEX 1 : COMMON NEIGHBOURS
      * FORMULA : intersect (neighbours1, neighbours2)
      */
    val commonNeighbours: Graph[_, Int] = pagerankGraph.commonNeighbours()
    // Graph where each edge is associated with number of common neighbours of vertices on edge

    println("COMMON NEIGHBOURS TRIPLETS :")
    commonNeighbours.triplets.foreach(println(_))

//    val CNtripletsDF = commonNeighbours.triplets.map(triplet => (triplet.srcId, triplet.srcAttr, triplet.dstId, triplet.dstAttr, triplet.attr))
//      .toDF("srcId", "srcAttr", "dstId", "dstAttr", "edgeAttr")
//    CNtripletsDF.write.json("CNOutputJSON.txt")

    commonNeighbours.triplets.saveAsTextFile("CNOut.txt")
    /**
      * INDEX 2 : ADAMIC ADAR
      * FORMULA : 1 / log (degree(common neighbours))
      */
    val adamicAdarGraph: Graph[_, Double] = pagerankGraph.adamicAdar(true)
    // Graph where each edge is associated with its Adamic Adar measure

    println("ADAMIC ADAR TRIPLETS :")
    adamicAdarGraph.triplets.foreach(println(_))

//    val AAtripletsDF = adamicAdarGraph.triplets.map(triplet => (triplet.srcId, triplet.srcAttr, triplet.dstId, triplet.dstAttr, triplet.attr))
//      .toDF("srcId", "srcAttr", "dstId", "dstAttr", "edgeAttr")
//    AAtripletsDF.write.json("AAOutputJSON.txt")

    adamicAdarGraph.triplets.saveAsTextFile("AAOut.txt")

    /**
      * INDEX 3 : RESOURCE ALLOCATION
      * FORMULA :
      */
    val resourceAllocationGraph: Graph[_, Double] = pagerankGraph.resourceAllocation(true)
    // Graph where each edge is associated with its Adamic Adar measure

    println("RESOURCE ALLOCATION TRIPLETS :")
    resourceAllocationGraph.triplets.foreach(println(_))

//    val RAtripletsDF = resourceAllocationGraph.triplets.map(triplet => (triplet.srcId, triplet.srcAttr, triplet.dstId, triplet.dstAttr, triplet.attr))
//      .toDF("srcId", "srcAttr", "dstId", "dstAttr", "edgeAttr")
//    RAtripletsDF.write.json("RAOutputJSON.txt")

    resourceAllocationGraph.triplets.saveAsTextFile("RAOut.txt")

    /**
      * INDEX 4 : PREFERENTIAL ATTACHMENT
      * FORMULA :
      */
    val preferentialAttacmentGraph: Graph[_, Int] = pagerankGraph.preferentialAttachment(true)
    // Graph where each edge is associated with its Adamic Adar measure

    println("PREFERENTIAL ATTACHMENT TRIPLETS :")
    preferentialAttacmentGraph.triplets.foreach(println(_))

//    val PAtripletsDF = preferentialAttacmentGraph.triplets.map(triplet => (triplet.srcId, triplet.srcAttr, triplet.dstId, triplet.dstAttr, triplet.attr))
//      .toDF("srcId", "srcAttr", "dstId", "dstAttr", "edgeAttr")
//    PAtripletsDF.write.json("PAOutputJSON.txt")

    preferentialAttacmentGraph.triplets.saveAsTextFile("PAOut.txt")

    /**
      * INDEX 5 : JACCARD INDEX
      * FORMULA :
      */
    val jaccardIndexGraph: Graph[_, Double] = pagerankGraph.jaccardIndex(true)
    // Graph where each edge is associated with its Adamic Adar measure

    println("JACCARD INDEX TRIPLETS :")
    jaccardIndexGraph.triplets.foreach(println(_))

//    val JItripletsDF = jaccardIndexGraph.triplets.map(triplet => (triplet.srcId, triplet.srcAttr, triplet.dstId, triplet.dstAttr, triplet.attr))
//      .toDF("srcId", "srcAttr", "dstId", "dstAttr", "edgeAttr")
//    JItripletsDF.write.json("JIOutputJSON.txt")

    jaccardIndexGraph.triplets.saveAsTextFile("JIOut.txt")

  }

}
