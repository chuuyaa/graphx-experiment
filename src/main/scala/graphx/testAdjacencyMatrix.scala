import scala.collection.mutable.ArrayBuffer

object TestGraph {

  class Graph(val vertex: IndexedSeq[String], edges: Seq[(Int, Int)]) {
    def size: Int = vertex.length
    val index: Map[String, Int] = vertex.zipWithIndex.toMap
    val adjacent = edges groupBy (_._1) mapValues (_ map (_._2))
    def adjacencyMatrix = adjacent mapValues (_.toSet) mapValues (0 to size map _)
    def printEdges: String = {
      for(idx <- 0 until size)
        yield f"vertex $idx: ${adjacent(idx) mkString " "}"
    } mkString "\n"
    def printAdjacencyList: String = adjacent mapValues (_ mkString ", ") mkString "\n"
    def printAdjacencyMatrix: String = adjacencyMatrix mapValues(_ mkString ", ") mkString "\n"
  }

  def main(args: Array[String]) {
    def vertices: Array[String] = Array("Seattle", "San Francisco", "Los Angeles",
      "Denver", "Kansas City", "Chicago", "Boston", "New York",
      "Atlanta", "Miami", "Dallas", "Houston")

    def edges: ArrayBuffer[(Int, Int)] = ArrayBuffer(
      (0, 1), (0, 3), (0, 5),
      (1, 0), (1, 2), (1, 3),
      (2, 1), (2, 3), (2, 4), (2, 10),
      (3, 0), (3, 1), (3, 2), (3, 4), (3, 5),
      (4, 2), (0, 9), (4, 5), (4, 7), (4, 8), (4, 10),
      (5, 0), (5, 3), (5, 4), (5, 6), (5, 7),
      (6, 5), (6, 7),
      (7, 4), (7, 5), (7, 6), (7, 8),
      (8, 4), (8, 7), (8, 9), (8, 10), (8, 11),
      (9, 8), (9, 11),
      (10, 2), (10, 4), (10, 8), (10, 11),
      (11, 8), (11, 9), (11, 10)
    )
    val graph = new Graph(vertices, edges)

    println("number of vertices in graph: " + graph.size)
    println("the vertex with index 1 is: " + graph.vertex(1))
    println("the index for Miami is: " + graph.index("Miami"))
    println("the edges for graph: ")
    println(graph.printEdges)
    println("adjacency list for graph: ")
    println(graph.printAdjacencyList)
    println("adjacency matrix for graph: ")
    println(graph.printAdjacencyMatrix)
  }
}