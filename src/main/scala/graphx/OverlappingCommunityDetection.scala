package graphx

import org.apache.spark.{HashPartitioner, SparkConf, SparkContext}
import org.apache.spark.api.java.JavaSparkContext
import org.apache.spark.graphx._
import org.apache.spark.sql.SparkSession

import scala.collection.mutable
import scala.reflect.ClassTag

object OverlappingCommunityDetection {

  def main(args: Array[String]): Unit = {


    //val spark = SparkSession.builder().master("local[*]").config("spark.sql.warehouse.dir", "file:///Users/User/IdeaProject/graphx-experiment/").getOrCreate().sparkContext

    val spark = new JavaSparkContext(new SparkConf().setAppName("labelPropagation").setMaster("local[*]"));
    spark.hadoopConfiguration().set("mapred.max.split.size", "10000");

    val graph = time{generateGraph(spark)}

    // find the overlapping communities with maxiteration 5 and max noOfCommunities per node 4
    val overlapCommunities = OverlappingCommunityDetection.run(graph,10,4)
    //print(overlapCommunities)
    //overlapCommunities.edges.foreach(println)

  }

  def time[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) + "ns")
    result
  }
  /**
    * Run Overlapping Community Detection for detecting overlapping communities in networks.
    *
    * OLPA is an overlapping community detection algorithm.It is based on standard Label propagation
    * but instead of single community per node , multiple communities can be assigned per node.
    *
    * @tparam ED the edge attribute type (not used in the computation)
    * @param graph           the graph for which to compute the community affiliation
    * @param maxSteps        the number of supersteps of OLPA to be performed. Because this is a static
    *                        implementation, the algorithm will run for exactly this many supersteps.
    * @param noOfCommunities 	the maximum number of communities to be assigned to each vertex
    * @return a graph with list of vertex attributes containing the labels of communities affiliation
    */

  def run[VD, ED: ClassTag](graph: Graph[VD, ED], maxSteps: Int, noOfCommunities: Int) = {
    require(maxSteps > 0, s"Maximum of steps must be greater than 0, but got ${maxSteps}")
    require(noOfCommunities > 0, s"Number of communities must be greater than 0, but got ${noOfCommunities}")

    val threshold: Double = 1.0 / noOfCommunities

    val lpaGraph: Graph[mutable.Map[VertexId, Double], ED] = graph.mapVertices { case (vid, _) => mutable.Map[VertexId, Double](vid -> 1) }

    def sendMessage(e: EdgeTriplet[mutable.Map[VertexId, Double], ED]): Iterator[(VertexId, mutable.Map[VertexId, Double])] = {
      Iterator((e.srcId, e.dstAttr), (e.dstId, e.srcAttr))
    }

    def mergeMessage(count1: mutable.Map[VertexId, Double], count2: mutable.Map[VertexId, Double])
    : mutable.Map[VertexId, Double] = {
      val communityMap: mutable.Map[VertexId, Double] = new mutable.HashMap[VertexId, Double]
      (count1.keySet ++ count2.keySet).map(key => {

        val count1Val = count1.getOrElse(key, 0.0)
        val count2Val = count2.getOrElse(key, 0.0)
        communityMap += (key -> (count1Val + count2Val))
      })
      communityMap
    }

    def vertexProgram(vid: VertexId, attr: mutable.Map[VertexId, Double], message: mutable.Map[VertexId, Double]): mutable.Map[VertexId, Double] = {
      if (message.isEmpty)
        attr
      else {
        var coefficientSum = message.values.sum

        //Normalize the map so that every node has total coefficientSum as 1
        val normalizedMap: mutable.Map[VertexId, Double] = message.map(row => {
          (row._1 -> (row._2 / coefficientSum))
        })


        val resMap: mutable.Map[VertexId, Double] = new mutable.HashMap[VertexId, Double]
        var maxRow: VertexId = 0L
        var maxRowValue: Double = Double.MinValue

        normalizedMap.foreach(row => {
          if (row._2 >= threshold) {
            resMap += row
          } else if (row._2 > maxRowValue) {
            maxRow = row._1
            maxRowValue = row._2
          }
        })

        //Add maximum value node in result map if there is no node with sum greater then threshold
        if (resMap.isEmpty) {
          resMap += (maxRow -> maxRowValue)
        }

        coefficientSum = resMap.values.sum
        resMap.map(row => {
          (row._1 -> (row._2 / coefficientSum))
        })
      }
    }

    val initialMessage = mutable.Map[VertexId, Double]()

    val overlapCommunitiesGraph = Pregel(lpaGraph, initialMessage, maxIterations = maxSteps)(
      vprog = vertexProgram,
      sendMsg = sendMessage,
      mergeMsg = mergeMessage)

    overlapCommunitiesGraph.mapVertices((vertexId, vertexProperties) => vertexProperties.keys)
  }

  /**
    * Load my user data and parse into tuples of user id and attribute list
    * Parse the edge data which is already in userId -> userId format
    * Finally, attach the user attributes to the edge data to form graph
    * @param spark
    */
  def generateGraph(spark: SparkContext) = {

    val path = "/Users/User/IdeaProject/input/50.txt"
    val user_path = "/Users/User/IdeaProject/input/users-test.txt"

    val users = spark.textFile(user_path).map{l => val lineSplits = l.split("\\s+")
      val id = lineSplits(0).trim.toLong
      val data = lineSplits.slice(1,lineSplits.length).mkString(" ")
      (id,data)}.cache()

    val relationships = GraphLoader.edgeListFile(spark, path, numEdgePartitions = 4).partitionBy(PartitionStrategy.EdgePartition1D)
    users.foreachPartition(f=>f.foreach(println))
    val partitioner = new HashPartitioner(10)

    val graph = relationships.outerJoinVertices(users){
      case(uid, name, Some(attrList)) => attrList
      case(uid, name, None) => "Missing data"
    }
    //graph.partitionBy(partitioner)
    graph
  }

}
