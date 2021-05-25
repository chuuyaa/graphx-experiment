package graphx.graphanalysis

import org.apache.spark.SparkContext
import org.apache.spark.graphx.{Graph, GraphLoader}

object GraphBuilder {

  def loadFromFile(sc: SparkContext, path: String): Graph[Int, Int] = {
    /**
      * edgeListFile : Loads a graph from an edge list formatted file
      * where each line contains two integers: a source id and a target id.
      */
    GraphLoader.edgeListFile(sc, path)
  }

}
