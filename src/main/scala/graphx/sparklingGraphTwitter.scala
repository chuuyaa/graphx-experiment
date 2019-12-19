package graphx

import java.util

import graphx.clustering.TransitiveClosure
import graphx.clustering.TransitiveClosure._
import org.apache.spark.api.java
import org.apache.spark.rdd._
import graphx.indices.OperatorsNew._
import org.apache.spark.SparkContext
import org.apache.spark.graphx.{EdgeRDD, Graph, GraphLoader, VertexId}
import org.apache.spark.mllib.clustering._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import scala.io.Source


object sparklingGraphTwitter extends Serializable {

//  var path_10k = "/user/hadoop/data/graphx/10k.txt";
//  var path_15k = "/user/hadoop/data/graphx/15k.txt";
//  var path_20k = "/user/hadoop/data/graphx/20k.txt";
//  var path_30k = "/user/hadoop/data/graphx/30k.txt";
//  var path_40k = "/user/hadoop/data/graphx/40k.txt";
//  var path_50k = "/user/hadoop/data/graphx/50k.txt";
//  var path_100k = "/user/hadoop/data/graphx/100k.txt";
//  var path_1mill = "/user/hadoop/data/graphx/twitter.txt";
//  var path_small = "/user/hadoop/data/graphx/twitter-small.txt";

  var path_10k = "/Users/User/IdeaProject/graphx-experiment/input/10k.txt";
  var path_15k = "/Users/User/IdeaProject/graphx-experiment/input/15k.txt";
  var path_20k = "/Users/User/IdeaProject/graphx-experiment/input/20k.txt";
  var path_30k = "/Users/User/IdeaProject/graphx-experiment/input/30k.txt";
  var path_40k = "/Users/User/IdeaProject/graphx-experiment/input/40k.txt";
  var path_50k = "/Users/User/IdeaProject/graphx-experiment/input/50k.txt";
  var path_100k = "/Users/User/IdeaProject/graphx-experiment/input/100k.txt";
  var path_1mill = "/Users/User/IdeaProject/graphx-experiment/input/twitter.txt";
  var path_small = "/Users/User/IdeaProject/graphx-experiment/input/twitter-small.txt";

  var path = path_10k
  def main(args: Array[String]): Unit = {

    // Spark entry point
//    val spark = SparkSession.builder().master("yarn").config("spark.sql.warehouse.dir", "file:///home/hadoop/graphx-experiment/").getOrCreate().sparkContext
    val spark = SparkSession.builder().master("local[*]").config("spark.sql.warehouse.dir", "file:///Users/User/IdeaProject/graphx-experiment/").getOrCreate().sparkContext

//        generateGraph(spark)
        val pagerankGraph = computePagerank(generateGraph(spark))
        computeSimilarityIndex(pagerankGraph)
  }

//  def filter_constraints(constraintsfile: String, similarities: Graph[Double,Double]) = {
//    //read constraints file
//    val sc = SparkSession.builder().master("local[*]").config("spark.sql.warehouse.dir", "file:///Users/User/IdeaProject/graphx-experiment/").getOrCreate().sparkContext
//    val f = sc.textFile(constraintsfile).map{l => val lineSplits = l.split("\\s+")
//      val src = lineSplits(0).trim.toLong
//      val dst = lineSplits(1).trim.toLong
//      val label = lineSplits(2).trim.toDouble
//      (src,dst,label)}
//    //filter with condition, if label -1, update similarity value of the edges by subtracting one and vice versa
//    val ss = similarities.edges.map(e=> (e.srcId.toLong, e.dstId.toLong, e.attr))
//    val newRdd = f.map(x => ((x._1,x._2), x._3))
//    val newsimRdd = ss.map(x => ((x._1,x._2), x._3))
//    val joined = newsimRdd.join(newRdd)
//    // return rdd of long, long, double of new similarity value
//    val t = joined.map(x => (x._1, (x._2._1+x._2._2)))
//    t.map(x=> (x._1._1, x._1._2, x._2))
//  }

  def compare(long1: Long, long2: Long, d: Double, RDD: RDD[(Long, Long, Double)]) ={
    RDD.foreach(
      i=>
        if(i._1.equals(long1) && i._2.equals(long2)){
          println("sini : " + i._3)
        }
    )
  }
  /**
    * Load my user data and parse into tuples of user id and attribute list
    * Parse the edge data which is already in userId -> userId format
    * Finally, attach the user attributes to the edge data to form graph
    * @param spark
  */
  def generateGraph(spark: SparkContext) = {
    val users = spark.textFile(path).map{l => val lineSplits = l.split("\\s+")
      val id = lineSplits(0).trim.toLong
      val data = lineSplits.slice(1,lineSplits.length).mkString(" ")
      (id,data)}

    val lines = spark.textFile(path)
    val relationships = GraphLoader.edgeListFile(spark, path)

    val graph = relationships.outerJoinVertices(users){
      case(uid, name, Some(attrList)) => attrList
      // Some users may not have attributes so we set them as empty
      case(uid, name, None) => "Missing data"
    }
    graph
  }

  def generateHalfFile(): Unit ={

  }
  /**
    * Compute the PageRank
    * Output :
    * ((11330027,1.7562175998195302),(17224642,6.040126131542945),0.00847457627118644)
    * ((568770231,0.22816618569378982),(113751683,2.2956495665192036),0.06666666666666667)
    * Explanation:
    * ((srcID, srcAttr),(dstId, dstAttr), edgeAttr))
    */
  def computePagerank(graph: Graph[String,Int]) = {
    val pagerankGraph = graph.pageRank(0.001)
    val ranks = graph.pageRank(0.0001).vertices
    pagerankGraph
  }

  def computeSimilarityIndex(pagerankGraph: Graph[Double,Double]) = {

    val commonNeighbours = pagerankGraph.commonNeighbours()
    val cnEdges = commonNeighbours.edges

    val adamicAdarGraph: Graph[_, Double] = pagerankGraph.adamicAdar(true)
    val aaEdges = adamicAdarGraph.edges

    val resourceAllocationGraph: Graph[_, Double] = pagerankGraph.resourceAllocation(true)
    val raEdges = resourceAllocationGraph.edges

    val combined2Edges = updateGraphProperty(cnEdges, aaEdges)
    val combined3Edges = updateGraphProperty(combined2Edges, raEdges)

    splitTrainTestGraph(combined3Edges)

    graphClustering(pagerankGraph, raEdges)

  }

  def updateGraphProperty(edges: EdgeRDD[_], edges2: EdgeRDD[_]) = {
    var joinedEdges = edges.innerJoin(edges2){
      case (srcId, dstId, graph1Attr, graph2Attr) => graph1Attr
    }
    joinedEdges
  }

  def splitTrainTestGraph(joinedEdges: EdgeRDD[_]) = {
    //        val Array(trainingGraph, testGraph) = joinedEdges.randomSplit(Array(0.6,0.4))
    //        println("trainingGraph")
    //        trainingGraph.foreach(println(_))
    //        println("testGraph")
    //        testGraph.foreach(println(_))
  }

  def graphLearningTask(spark: SparkContext) = {

  }

  def graphClustering(pagerankGraph: Graph[Double,Double], aaEdges: EdgeRDD[Double]) = {
    val aaCombinedGraph = Graph(pagerankGraph.vertices, aaEdges)
    aaCombinedGraph.triplets.foreach(println(_))

//    filter_constraints(path_small, aaCombinedGraph)

    val adamicadarModel =
      new PowerIterationClusteringNew()
        .setK(10)
        .setMaxIterations(15)
        .setInitializationMode("degree")
        .run(aaCombinedGraph)

    val adamicadarClusters = adamicadarModel.assignments.collect().groupBy(_.cluster).mapValues(_.map(_.id))
    val assignments2 = adamicadarClusters.toList.sortBy{ case (k,v) => v.length}
    val assignments2Str = assignments2
      .map { case (k,v) =>
        s"$k -> ${v.sorted.mkString("[", ",", "]")}"
      }.mkString(", ")
    val sizes2Str = assignments2.map {
      _._2.length
    }.sorted.mkString("(", ",", ")")
    println(s"adamic adar cluster: $assignments2Str\n" + s"adamic adar cluster sizes: $sizes2Str")
  }

  def outputLinkPrediction(spark: SparkContext) = {
    /**
      * ((566386538, name1),(307458983, name2),1)
      * ((srcId, srcAttr),(dstId, dstAttr), edgeAttr) edge attriv=bute by default is 1
      */
    println("GRAPH TRIPLETS :")
    generateGraph(spark).triplets.foreach(println(_))

    //    println("PAGERANK GRAPH TRIPLETS :")
    //    pagerankGraph.triplets.foreach(println(_))

    /**
      * print out all similarity index computed
      */
    //    println("COMMON NEIGHBOURS TRIPLETS :")
    //    commonNeighbours.edges.foreach(println(_))
  }

}
