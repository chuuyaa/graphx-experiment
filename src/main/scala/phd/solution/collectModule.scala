package phd.solution

import graphx.indices.OperatorsNew._
import org.apache.spark
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.api.java.JavaSparkContext
import org.apache.spark.graphx.{EdgeRDD, Graph, GraphLoader, PartitionStrategy}
import org.apache.spark.mllib.clustering._
import org.apache.spark.scheduler.{SparkListener, SparkListenerStageCompleted}
import org.apache.spark.sql.SparkSession

/**
  * the idea:
  * right after the application is submitted through sparkcontext, we will code to get any parameter we can
  * eg; sparkcontext.getSchedulingMode, .getDataSize, .getDataType, .. (TO FIGURE OUT WHY)
  */
object collectModule extends Serializable {

    var path_10k = "/Users/User/IdeaProject/input/10k.txt"
    var path_50k = "/Users/User/IdeaProject/graphx-experiment/input/50k.txt"
    var path_100k = "/Users/User/IdeaProject/input/50.txt"

    var path = path_100k
    def main(args: Array[String]): Unit = {

      val spark = new JavaSparkContext(new SparkConf().setAppName("testCollectModule").setMaster("local[*]"));

      /**
        * by the time we initiate sparkcontext
        */
      //UNIQUE ID FOR SPARK APPLICATION
      println("APP ID : "+spark.applicationId)
      //GET APPLICATION NAME
      println("APP NAME : "+spark.appName)
      //GET DEPLOY MODE, EITHER CLIENT OR CLUSTER MODE https://techvidvan.com/tutorials/spark-modes-of-deployment/
      println("DEPLOY MODE : "+spark.deployMode)
      //RETURN ALL POOLS FOR FAIR TYPE SCHEDULER, POOL IS A SCHEDULABLE ENTITY
      println("POOL : "+spark.getAllPools)
      //GET DIR UNDER WHICH RDDS ARE GOING TO BE CHECKPOINTED, IF ANY
      println("CHECKPOINT DIRECTORY : "+spark.getCheckpointDir)
      //RETURN A COPY OF SPARKCONTEXT CONFIG
      println("SPARK CONFIGURATION : "+spark.getConf)
      //RETURN A MAP FROM SLAVE ABOUT MEM AVAILABLE FOR CACHING, REMAINING MEMORY FOR CACHING
      println("EXECUTOR MEMORY STATUS : "+spark.getExecutorMemoryStatus.mkString(""))
      //RETURN INFORMATION ABOUT BLOCKS STORED IN ALL OF THE SLAVES
      println("EXECUTOR STORAGE STATUS : "+spark.getExecutorStorageStatus)
      //RETURNS A JAVA MAP OF JAVARDDS THAT HAVE MARKED THEMSELVES AS PERSISTENT VIA CACHE() CALL
      println("PERSISTENT RDDs : "+spark.getPersistentRDDs)
      //RETURN INFORMATION ABOUT WHAT RDDS ARE CACHED, IF THEY ARE IN MEM OR ON DISK, HOW MUCH SPACE THEY TAKE, ETC
      println("RDD STORAGE INFO : "+spark.getRDDStorageInfo.foreach(println))
      //CURRENT SCHEDULING MODE
      println("SCHEDULING MODE : "+spark.getSchedulingMode)
      //GET DIRECTORY OF SPARK HOME
      println("SPARK HOME LOCATION : "+spark.getSparkHome())
      //CLUSTER URL TO CONNECT TO (E.G. MESOS://HOST:PORT, SPARK://HOST:PORT, LOCAL[4]).
      println("MASTER URL TO CONNECT TO : "+spark.master)
      //LOW-LEVEL STATUS REPORTING APIS FOR MONITORING JOB AND STAGE PROGRESS
      println("STATUS TRACKER (active jobs) : "+spark.statusTracker.getActiveJobIds())
      println("STATUS TRACKER (active stages) : "+spark.statusTracker.getActiveStageIds())

      /**
        * by the time we want to start ...? action or job or what? i guess action
        */

    }

    /**
      * Load my user data and parse into tuples of user id and attribute list
      * Parse the edge data which is already in userId -> userId format
      * Finally, attach the user attributes to the edge data to form graph
      * @param spark
      */
    def generateGraph(spark: SparkContext) = {
      val users = spark.textFile(path,4).map{l => val lineSplits = l.split("\\s+")
        val id = lineSplits(0).trim.toLong
        val data = lineSplits.slice(1,lineSplits.length).mkString(" ")
        (id,data)}.cache()

      val lines = spark.textFile(path,4)
      val relationships = GraphLoader.edgeListFile(spark, path).partitionBy(PartitionStrategy.RandomVertexCut, 4)

      val graph = relationships.outerJoinVertices(users){
        case(uid, name, Some(attrList)) => attrList
        case(uid, name, None) => "Missing data"
      }
      graph
    }

}
