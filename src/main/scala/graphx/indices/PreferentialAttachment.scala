package graphx.indices

import ml.sparkling.graph.api.operators.measures.EdgeMeasure
import ml.sparkling.graph.operators.measures.utils.NeighboursUtils
import ml.sparkling.graph.operators.measures.utils.NeighboursUtils.{NeighbourSet}
import org.apache.spark.graphx.Graph

import scala.reflect.ClassTag

object PreferentialAttachment extends EdgeMeasure[Int, NeighbourSet]{
  override def computeValue(srcAttr: NeighbourSet, dstAttr: NeighbourSet, treatAsUndirected: Boolean=false): Int ={
    (srcAttr.size())*(dstAttr.size())
  }

  override def preprocess[VD, E](graph: Graph[VD, E], treatAsUndirected: Boolean=false)(implicit evidence$2: ClassTag[VD], evidence$3: ClassTag[E]): Graph[NeighbourSet, E] = {
    NeighboursUtils.getWithNeighbours(graph,treatAsUndirected)
  }
}
