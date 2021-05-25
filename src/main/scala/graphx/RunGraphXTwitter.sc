import org.apache.spark.SparkContext
import org.apache.spark.sql.{SQLContext, SparkSession}

/**
  * steps
  * step 1
  * Read input data, EDGELISTS
  * step 2
  * generate graph, using GraphX
  * step 3
  * compute link prediction method to acquire prediction scores (AA, JC, PA, SC, N2VEC)
  * step 4
  * output the results (runtime vs datasize, runtime vs partition, accuracy?)
  */

/**
  * scala>
  * // Change labshome to the appropriate value for your computer
  * val labshome = "/home/datascience/lab10"
  * val edgeGraph = GraphLoader.edgeListFile(sc, s"${labshome}/lab10_data/edges.txt")
  *
  * This parses the edge list file and creates a `Graph` object. However, this graph doesn't have any vertex properties, so we don't know which vertex corresponds to which article.
  *
  * The vertex file contains this information. The vertex file is formatted so that the first item on each line is the vertex ID, and the rest of the line is the article title that this vertex corresponds to. We will use Spark to parse the vertex file:
  *
  * scala>
  * val verts = sc.textFile(s"${labshome}/lab10_data/verts.txt").map {l =>
  * val lineSplits = l.split("\\s+")
  * val id = lineSplits(0).trim.toLong
  * val data = lineSplits.slice(1, lineSplits.length).mkString(" ")
  * (id, data)
  * }
  */
object RunGraphXTwitter extends Serializable {

  def main(args: Array[String]): Unit = {

    // Spark entry point
    val spark = SparkSession.builder().master("yarn").config("spark.sql.warehouse.dir", "file:///Users/User/IdeaProject/graphx-cy/oreilly-graphx/").getOrCreate().sparkContext
    //tryAgainGraph(spark)

  }

  private def simpleGraphOperations(spark: SparkContext) = {
    //read data
    val sc = spark
    val sqlContext = new SQLContext(sc)
    val file = sc.textFile("graphx-cy/oreilly-graphx/src/main/scala/com/spark/graphx/twitter/twitter.txt")

    val df = sqlContext.read
  }

  private def structuralOperationsTwitter(spark: SparkContext) = {
    // step 1 : create edge RDD
    // step 2 : create vertex RDD
    // step 3 : Define a default user in case there are relationship with missing user
    // step 4 : Build initial graph
  }
}