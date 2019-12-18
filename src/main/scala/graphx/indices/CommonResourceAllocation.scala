//package graphx.indices
//
//import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
//import ml.sparkling.graph.api.operators.algorithms.shortestpaths.ShortestPathsTypes
//import ml.sparkling.graph.api.operators.measures.EdgeMeasure
//import ml.sparkling.graph.operators.measures.utils.CollectionsUtils._
//import ml.sparkling.graph.operators.measures.utils.NeighboursUtils
//import ml.sparkling.graph.operators.measures.utils.NeighboursUtils.{NeighbourSet, NeighboursMap}
//import org.apache.spark.graphx.Graph
//
//import scala.reflect.ClassTag
//
//object CommonResourceAllocation extends EdgeMeasure[Double, NeighboursMap]{
//  override def computeValue(srcAttr: NeighboursMap, dstAttr: NeighboursMap, treatAsUndirected: Boolean): Double = {
//    val commonNeighbours=intersect(srcAttr.keySet(),dstAttr.keySet())
//
//    val jSet = new ShortestPathsTypes.JSet[Long]()
//    val intersectU = intersect(srcAttr.keySet(), srcAttr.keySet() + dstAttr.keySet())
//    val resourceallocation = commonNeighbours.toList.map(id=>srcAttr.get(id).size()).map(1.0 /_).sum
//
//  }
//
//  override def preprocess[VD, E](graph: Graph[VD, E], treatAsUndirected: Boolean)(implicit evidence$2: ClassTag[VD], evidence$3: ClassTag[E]): Graph[NeighboursMap, E] = {
//
//  }
//}
