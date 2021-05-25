package gclustering

import graphx.indices.OperatorsNew.DSL
import org.apache.spark.api.java.JavaSparkContext
import org.apache.spark.graphx.{EdgeRDD, Graph, GraphLoader}
import org.apache.spark.mllib.clustering.PowerIterationClusteringNew
import org.apache.spark.{SparkConf, SparkContext}

object sparklingGraphTwitter extends Serializable {

  //var path_100k = "/user/hadoop/data/graphx/100k.txt";
  //val spark = new JavaSparkContext(new SparkConf().setAppName("sparklingGraphTwitter").setMaster("yarn"));

  // [UNCOMMENT FOR TESTING]
  var path_100k = "/Users/User/IdeaProject/input/50k.txt"
  val spark = new JavaSparkContext(new SparkConf().setAppName("sparklingGraphTwitter").setMaster("local[*]"));

  var path = path_100k

  def main(args: Array[String]): Unit = {
    spark.hadoopConfiguration().set("mapred.max.split.size", "10000");
    val pagerankGraph = computePagerank(generateGraph(spark))
    computeSimilarityIndex(pagerankGraph)
  }

  /**
    * Load my user data and parse into tuples of user id and attribute list
    * Parse the edge data which is already in userId -> userId format
    * Finally, attach the user attributes to the edge data to form graph
    *
    * @param spark
    */
  def generateGraph(spark: SparkContext) = {
    val users = spark.textFile(path).map { l =>
      val lineSplits = l.split("\\s+")
      val id = lineSplits(0).trim.toLong
      val data = lineSplits.slice(1, lineSplits.length).mkString(" ")
      (id, data)
    }.cache()

    val lines = spark.textFile(path)
    val relationships = GraphLoader.edgeListFile(spark, path)

    val graph = relationships.outerJoinVertices(users) {
      case (uid, name, Some(attrList)) => attrList
      case (uid, name, None) => "Missing data"
    }
    graph
  }

  /**
    * Compute the PageRank
    * Output :
    * ((11330027,1.7562175998195302),(17224642,6.040126131542945),0.00847457627118644)
    * ((568770231,0.22816618569378982),(113751683,2.2956495665192036),0.06666666666666667)
    * Explanation:
    * ((srcID, srcAttr),(dstId, dstAttr), edgeAttr))
    */
  def computePagerank(graph: Graph[String, Int]) = {
    val pagerankGraph = graph.pageRank(0.001)
    val ranks = graph.pageRank(0.0001).vertices
    pagerankGraph
  }

  def computeSimilarityIndex(pagerankGraph: Graph[Double, Double]) = {

    val commonNeighbours = pagerankGraph.commonNeighbours()
    val cnEdges = commonNeighbours.edges

    val adamicAdarGraph: Graph[_, Double] = pagerankGraph.adamicAdar(true)
    val aaEdges = adamicAdarGraph.edges

    val resourceAllocationGraph: Graph[_, Double] = pagerankGraph.resourceAllocation(true)
    val raEdges = resourceAllocationGraph.edges

    val combined2Edges = updateGraphProperty(cnEdges, aaEdges)
    val combined3Edges = updateGraphProperty(combined2Edges, raEdges)

    graphClustering(pagerankGraph, raEdges)
  }

  def updateGraphProperty(edges: EdgeRDD[_], edges2: EdgeRDD[_]) = {
    var joinedEdges = edges.innerJoin(edges2) {
      case (srcId, dstId, graph1Attr, graph2Attr) => graph1Attr
    }
    joinedEdges
  }

  def graphClustering(pagerankGraph: Graph[Double, Double], aaEdges: EdgeRDD[Double]) = {
    val aaCombinedGraph = Graph(pagerankGraph.vertices, aaEdges)
    //aaCombinedGraph.triplets.foreach(println(_))

    val adamicadarModel =
      new PowerIterationClusteringNew()
        .setK(10)
        .setMaxIterations(15)
        .setInitializationMode("degree")
        .run(aaCombinedGraph)

    val adamicadarClusters = adamicadarModel.assignments.collect().groupBy(_.cluster).mapValues(_.map(_.id))
  }
}
