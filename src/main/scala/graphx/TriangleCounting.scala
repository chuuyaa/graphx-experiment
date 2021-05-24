package graphx

// record time taken
// for each of partitionig=ng methods

// $example on$
import java.io.InputStreamReader

import graphx.sparklingGraphTwitter.path
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.api.java.JavaSparkContext
import org.apache.spark.deploy.SparkSubmit
import org.apache.spark.graphx.{GraphLoader, PartitionID, PartitionStrategy, VertexId}
import org.apache.spark.util.SizeEstimator

import scala.collection.mutable
import scala.io.Source
// $example off$
import org.apache.spark.sql.SparkSession
object TriangleCounting {
  def main(args: Array[String]): Unit = {
    // Creates a SparkSession.
    val sc = new JavaSparkContext(new SparkConf().setAppName("sparklingGraphTwitter").setMaster("local[*]"));
    val spark = SparkSession
      .builder
      .appName(s"${this.getClass.getSimpleName}")
      .getOrCreate()

    time{generateGraph(sc)}
    // $example off$
    spark.stop()
  }

  def time[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    println("Elapsed time: " + (t1 - t0) + "ns")
    result
  }

  def generateGraph(spark: SparkContext) = {

    // $example on$
    // Load the edges in canonical order and partition the graph for triangle count
    /**
      * Note that partitionBy() is a transformation, so it always returns a new RDD.
      * Therefore it is important to persist otherwise, partitioning is repeatedly applied,
      * which involves shuffling each time the RDD is used.
      */
    val graph = GraphLoader.edgeListFile(spark, "followers.txt", true)
      .partitionBy(Test1).persist()
    // Find the triangle count for each vertex
    val triCounts = graph.triangleCount().vertices
    // Join the triangle counts with the usernames
    /**
      * Before using a map or Flatmap over a large transformed RDD its good to cache that RDD.
      */
    val users = spark.textFile("users.txt").map { line =>
      val fields = line.split(",")
      (fields(0).toLong, fields(1))
    }.cache()
    println(users.count())
    println(SizeEstimator.estimate(users))
    println(SizeEstimator.estimate(graph))
    println(users.getNumPartitions)

    val triCountByUsername = users.join(triCounts).map { case (id, (username, tc)) =>
      (username, tc)
    }
    // Print the result
    println(triCountByUsername.collect().mkString("\n"))

  }
}

case object Test1 extends PartitionStrategy {
  override def getPartition(src: VertexId, dst: VertexId, numParts: PartitionID): PartitionID = {

    println(src , dst, numParts)

    val ceilSqrtNumParts: PartitionID = math.ceil(math.sqrt(numParts)).toInt
    val mixingPrime: VertexId = 1125899906842597L
    if (numParts == ceilSqrtNumParts * ceilSqrtNumParts) {
      // Use old method for perfect squared to ensure we get same results
      val col: PartitionID = (math.abs(src * mixingPrime) % ceilSqrtNumParts).toInt
      val row: PartitionID = (math.abs(dst * mixingPrime) % ceilSqrtNumParts).toInt
      (col * ceilSqrtNumParts + row) % numParts

    } else {
      // Otherwise use new method
      val cols = ceilSqrtNumParts
      val rows = (numParts + cols - 1) / cols
      val lastColRows = numParts - rows * (cols - 1)
      val col = (math.abs(src * mixingPrime) % numParts / rows).toInt
      val row = (math.abs(dst * mixingPrime) % (if (col < cols - 1) rows else lastColRows)).toInt
      col * rows + row

    }
  }
}


case object CuyaTryMetisPartition extends PartitionStrategy{
  val metisMap = new mutable.HashMap[Int, Int]
  def loadMetisFile(): Unit ={
    var id = 1
    val path = "followers.txt"
    val fSource = Source.fromFile(path)
    for(line<-fSource.getLines){
      if(!"".equals(line)){
        metisMap.put(id, line.toInt)
        id = id +1
      }
    }
    fSource.close()
    println("metisMap size: "+ metisMap.size)
    metisMap.foreach{case (e,i) => println(e,i)}
  }
  override def getPartition(src: VertexId, dst: VertexId, numParts: PartitionID): PartitionID = {
    var s = metisMap.get(src.hashCode().toInt)
    if(!s.isEmpty)
      return s.getOrElse(0)
    else{
      println("src: " +src+", hascode: " + src.hashCode()+", partition: "+metisMap.get(src.hashCode()) + ", size: "+metisMap.size)
      throw new IllegalArgumentException("Metis can't find partition!")
    }

  }
}
