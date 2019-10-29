package graphx.indices

import ml.sparkling.graph.api.operators.measures.EdgeMeasure
import ml.sparkling.graph.operators.measures.utils.CollectionsUtils._
import ml.sparkling.graph.operators.measures.utils.NeighboursUtils
import ml.sparkling.graph.operators.measures.utils.NeighboursUtils.{NeighbourSet, NeighboursMap}
import org.apache.spark.graphx.Graph

import scala.reflect.ClassTag

object JaccardIndex extends EdgeMeasure[Double, NeighboursMap]{
  override def computeValue(srcAttr: NeighboursMap, dstAttr: NeighboursMap, treatAsUndirected: Boolean=false): Double = {
    val commonNeighbours=intersect(srcAttr.keySet(),dstAttr.keySet()).size
    val unionNeighbours=srcAttr.size()+dstAttr.size()
    commonNeighbours/unionNeighbours
  }

  override def preprocess[VD:ClassTag, E:ClassTag](graph: Graph[VD, E], treatAsUndirected: Boolean=false): Graph[NeighboursMap, E] = {
    NeighboursUtils.getWithSecondLevelNeighbours(graph,treatAsUndirected)
  }
}
