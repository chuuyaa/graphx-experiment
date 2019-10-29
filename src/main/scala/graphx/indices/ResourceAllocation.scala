package graphx.indices

import ml.sparkling.graph.api.operators.measures.EdgeMeasure
import ml.sparkling.graph.operators.measures.utils.CollectionsUtils._
import ml.sparkling.graph.operators.measures.utils.NeighboursUtils
import ml.sparkling.graph.operators.measures.utils.NeighboursUtils._
import org.apache.spark.graphx.Graph

import scala.reflect.ClassTag

object ResourceAllocation extends EdgeMeasure[Double, NeighboursMap] {
  def computeValue(srcAttr:NeighboursMap,dstAttr:NeighboursMap,treatAsUndirected:Boolean=false):Double={
    val commonNeighbours=intersect(srcAttr.keySet(),dstAttr.keySet())
    commonNeighbours.toList.map(id=>srcAttr.get(id).size()).map(1.0 /_).sum
  }

  override def preprocess[VD:ClassTag,E:ClassTag](graph: Graph[VD, E],treatAsUndirected:Boolean=false): Graph[NeighboursMap, E] = {
    NeighboursUtils.getWithSecondLevelNeighbours(graph,treatAsUndirected)
  }

}
