package graphanalysis

import org.apache.spark.SparkContext
import org.apache.spark.graphx.util.GraphGenerators
import org.apache.spark.graphx.{Edge, Graph, VertexId, VertexRDD}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.log4j._

object RunGraph {

  /**
    * def in Scala is used to define a function
    * main - this is our fundtion name
    * (agrs: Array[String]) Our main function takes in a named parameter args which is an
    * Array of String
    * : Unit = In Scala, the Unit keyword is used to define a function which does not return anything
    * this is similar to void keyword in Java
    * @param args
    */
  def main(args: Array[String]): Unit = {

    // Set log file to only see print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    // declare spark entry point which is a sparkContext
    // sparkContext is created using SparkSession.builder()
    val spark = SparkSession.builder().master("local[2]").getOrCreate().sparkContext
    System.setProperty("hadoop.home.dir", "C:/winutils.exe")
    print("simpleOperations:")
    //simpleGraphOperations(spark)
    //println("structuralOperations:")
    structuralOperations(spark)
    //print("neighbourhoodAggregation:")
    //neighbourhoodAggregation(spark)

  }

  private def simpleGraphOperations(spark: SparkContext) = {

    // defining vertices
    // vertex can be defined as an RDD tuple which in the tuple the elements are the vertexID and the properties
    val users: RDD[(VertexId, (String, String))] =
      spark.parallelize(Array(
        (3L, ("lin", "student")),
        (7L, ("ali", "postdoc")),
        (5L, ("bad", "prof")),
        (2L, ("fiza", "prof"))
      ))

    // defining edges
    // edge can be defined as RDD of Edge of String, so edge is an object that has a type property
    val relationships: RDD[Edge[String]] =
      spark.parallelize(Array(
        Edge(3L, 7L, "collab"), //[3L] -> collab -> [7L]
        Edge(5L, 3L, "advisor"), //5L -> advisor -> 3L
        Edge(2L, 5L, "colleague"), //2L -> colleague -> 5L
        Edge(5L, 7L, "colleague") //5L; ->>
      ))

    // build the initial Graph using Graphx.scala package
    // create a graph of a tuple and inside is a tuple of vertex property and also edge property
    // users = vertices && relationships = edges
    val graph: Graph[(String, String), String] = Graph(users, relationships)

    // Count all users which are postdocs
    val countOfPostdoc = graph.vertices.filter {
      case (id, (name, pos)) => pos == "postdoc"
    }.count()
    println(s"countOfPostdocs: $countOfPostdoc")

    // Count all the edges where src > dst
    val c = graph.edges.filter(e => e.srcId > e.dstId).count
    println(s"countOfSrc>Dst: $c")

    graph.edges.filter{
      case Edge(src, dst, prop) => src > dst
    }.count

    // Use the triplets view to create an RDD of facts.
    val facts: RDD[String] =
      graph.triplets.map(triplet => triplet.srcAttr._1 + " is the " + triplet.attr + " of " + triplet.dstAttr._1)
    facts.collect.foreach(println(_))
  }

  def structuralOperations(sc: SparkContext): Unit = {
    // Create an RDD for the vertices
    val users: RDD[(VertexId, (String, String))] =
      sc.parallelize(Array(
        (3L, ("lin", "student")),
        (7L, ("ali", "postdoc")),
        (5L, ("bad", "prof")),
        (2L, ("fiza", "prof")),
        (4L, ("peter", "student"))
      ))

    // Create an RDD for edges
    val relationships: RDD[Edge[String]] =
      sc.parallelize(Array(
        Edge(3L, 7L, "collab"), //[3L] -> collab -> [7L]
        Edge(5L, 3L, "advisor"), //5L -> advisor -> 3L
        Edge(2L, 5L, "colleague"), //2L -> colleague -> 5L
        Edge(5L, 7L, "pi"),
        Edge(4L, 0L, "student"),
        Edge(5L, 0L, "colleague")
      ))

    // Define a default user in case there are relationship with missing user
    val defaultUser = ("John", "Missing")

    //Build the initial Graph
    val graph = Graph(users, relationships, defaultUser)
    graph.triplets.foreach(println(_))

    // Notice that there is a user 0 (for which we have no information) connected to users
    // 4 peter and 5 franklin


    println("Connected Component")
    connectedComponents(graph)
    println("Subgraph")
    subgraph(graph)
  }

  def connectedComponents(graph: Graph[(String, String), String]) = {
    // Run Connected Components
    val ccGraph = graph.connectedComponents() // No longer contains missing field
    println(ccGraph)
    ccGraph.triplets.map(
      triplet => triplet.srcAttr + " is the " + triplet.attr + " of " + triplet.dstAttr
    ).collect.foreach(println(_))
  }

  private def subgraph(graph: Graph[(String, String), String]) = {
    graph.triplets.map(
      triplet => triplet.srcAttr._1 + " is the " + triplet.attr + " of " + triplet.dstAttr._2
    ).collect.foreach(println(_))

    // Remove missing vertices as well as the edges to connected to them
    val validGraph = graph.subgraph(vpred = (id, attr) => attr._2 != "Missing")

    // The valid subgraph will disconnect users 4 and 5 by removing user 0
    validGraph.vertices.collect.foreach(println(_))
    validGraph.triplets.map(
      triplet => triplet.srcAttr._1 + " is the " + triplet.attr + " of " + triplet.dstAttr._2
    ).collect.foreach(println(_))

  }

  def neighbourhoodAggregation(sc: SparkContext): Unit = {
    // Create a graph with 'age' as the vertex property.
    // Here we use a random graph for simplicity.
    val graph: Graph[Double, Int] =
    GraphGenerators.logNormalGraph(sc, numVertices = 100).mapVertices((id, _) => id.toDouble)

    // Compute the number of older followers and their total age
    val olderFollowers: VertexRDD[(Int,Double)] = graph.aggregateMessages[(Int, Double)](
      triplet => { // Map Function
        if (triplet.srcAttr > triplet.dstAttr){
          // Send message to destination vertex containing counter and age
          triplet.sendToDst(1, triplet.srcAttr)
        }
      },

      // Add counter and age
      (a,b) => (a._1 + b._1, a._2 + b._2) // Reduce Function
    )

    // Divide total age by number of older followers to get average age of older followers
    val avgAgeOfOlderFollowers: VertexRDD[Double] =
      olderFollowers.mapValues((id,value) =>
      value match {
        case (count, totalAge) => totalAge / count
      })

    // Display the results
    avgAgeOfOlderFollowers.collect.foreach(println(_))
  }
}
