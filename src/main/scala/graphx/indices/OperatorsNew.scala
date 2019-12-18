package graphx.indices

import ml.sparkling.graph.api.operators.algorithms.community.CommunityDetection.{CommunityDetectionAlgorithm, CommunityDetectionMethod, ComponentID}
import ml.sparkling.graph.api.operators.measures.{EdgeMeasure, VertexMeasureConfiguration}
import ml.sparkling.graph.operators.algorithms.coarsening.labelpropagation.LPCoarsening
import ml.sparkling.graph.operators.algorithms.community.pscan.PSCAN.computeConnectedComponents
import ml.sparkling.graph.operators.algorithms.link.BasicLinkPredictor
import ml.sparkling.graph.operators.measures.edge.{AdamicAdar, CommonNeighbours}
import ml.sparkling.graph.operators.measures.graph.{FreemanCentrality, Modularity}
import ml.sparkling.graph.operators.measures.utils.NeighboursUtils._
import ml.sparkling.graph.operators.measures.vertex.closenes.Closeness
import ml.sparkling.graph.operators.measures.vertex.clustering.LocalClustering
import ml.sparkling.graph.operators.measures.vertex.eigenvector.EigenvectorCentrality
import ml.sparkling.graph.operators.measures.vertex.hits.Hits
import ml.sparkling.graph.operators.measures.vertex.{Degree, NeighborhoodConnectivity, VertexEmbeddedness}
import ml.sparkling.graph.operators.partitioning.CommunityBasedPartitioning.{partitionGraphBy, partitionGraphUsing}
import org.apache.spark.SparkContext
import org.apache.spark.graphx.Graph

import scala.reflect.ClassTag

object OperatorsNew {

  implicit class ModularityDSL[E:ClassTag](graph:Graph[ComponentID,E]){
    def modularity()=Modularity.compute(graph)
  }

  implicit class DSL[VD:ClassTag ,ED:ClassTag](graph:Graph[VD,ED]){
    def PSCAN(epsilon:Double=0.1)=
      computeConnectedComponents(graph,epsilon)

    def LPCoarse(treatAsUndirected:Boolean=false)=LPCoarsening.coarse(graph,treatAsUndirected = treatAsUndirected)

    def closenessCentrality(vertexMeasureConfiguration: VertexMeasureConfiguration[VD, ED]=VertexMeasureConfiguration())(implicit num:Numeric[ED])=
      Closeness.compute(graph,vertexMeasureConfiguration)

    def eigenvectorCentrality(vertexMeasureConfiguration: VertexMeasureConfiguration[VD, ED]=VertexMeasureConfiguration())(implicit num:Numeric[ED])=
      EigenvectorCentrality.compute(graph,vertexMeasureConfiguration)

    def hits(vertexMeasureConfiguration: VertexMeasureConfiguration[VD, ED]=VertexMeasureConfiguration())(implicit num:Numeric[ED])=
      Hits.compute(graph,vertexMeasureConfiguration)

    def degreeCentrality(vertexMeasureConfiguration: VertexMeasureConfiguration[VD, ED]=VertexMeasureConfiguration())(implicit num:Numeric[ED])=
      Degree.compute(graph,vertexMeasureConfiguration)

    def neighborhoodConnectivity(vertexMeasureConfiguration: VertexMeasureConfiguration[VD, ED]=VertexMeasureConfiguration())(implicit num:Numeric[ED])=
      NeighborhoodConnectivity.compute(graph,vertexMeasureConfiguration)

    def vertexEmbeddedness(vertexMeasureConfiguration: VertexMeasureConfiguration[VD, ED]=VertexMeasureConfiguration())(implicit num:Numeric[ED])=
      VertexEmbeddedness.compute(graph,vertexMeasureConfiguration)

    def localClustering(vertexMeasureConfiguration: VertexMeasureConfiguration[VD, ED]=VertexMeasureConfiguration())(implicit num:Numeric[ED])=
      LocalClustering.compute(graph,vertexMeasureConfiguration)

    def freemanCentrality()=FreemanCentrality.compute(graph)

    def partitionBy(communityDetectionMethod:CommunityDetectionMethod[VD,ED])(implicit sc:SparkContext)=
      partitionGraphBy(graph,communityDetectionMethod)

    def partitionBy(communityDetectionMethod:CommunityDetectionAlgorithm,numParts:Int= -1)(implicit sc:SparkContext)=
      partitionGraphUsing(graph,communityDetectionMethod,numParts)

    def adamicAdar(treatAsUndirected:Boolean=false)={
      AdamicAdar.computeWithPreprocessing(graph,treatAsUndirected)
    }
    def adamicAdarTst(treatAsUndirected:Boolean=false)={
      val neighbour = AdamicAdar.computeWithPreprocessing(graph,treatAsUndirected)
      AdamicAdar.computeValue(new NeighboursMap(0), new NeighboursMap(0), treatAsUndirected)
    }
    def aaTestConvertOutputType(graph: Graph[_,ED]): Unit ={

    }

    def commonNeighbours(treatAsUndirected:Boolean=false)={
      CommonNeighbours.computeWithPreprocessing(graph,treatAsUndirected)
    }

    def predictLinks[EV: ClassTag, EO: ClassTag]( edgeMeasure: EdgeMeasure[EO, EV],threshold: EO,treatAsUndirected:Boolean=false)(implicit num: Numeric[EO]) = {
      BasicLinkPredictor.predictLinks(graph, edgeMeasure, threshold, treatAsUndirected)
    }

    def resourceAllocation(treatAsUndirected:Boolean=false)={
      ResourceAllocation.computeWithPreprocessing(graph,treatAsUndirected)
    }

    def preferentialAttachment(treatAsUndirected:Boolean=false)={
      PreferentialAttachment.computeWithPreprocessing(graph,treatAsUndirected)
    }

    def jaccardIndex(treatAsUndirected:Boolean=false)={
      JaccardIndex.computeWithPreprocessing(graph,treatAsUndirected)
    }
  }
}
