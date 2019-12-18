//import TestGraph.Graph
//import org.apache.spark.graphx.{EdgeContext, TripletFields, VertexRDD}
//import org.apache.spark.sql.Row
//
//object PageRank {
//
//  case class VertexAttr(srcId: Long, outDegree: Int, pageScore: Double)
//
//  case class PageMsg(pageScore: Double)
//
//  def reducer(a: PageMsg, b: PageMsg): PageMsg = PageMsg(a.pageScore + b.pageScore)
//
//  def runPageRank(g: GraphFrame, resetProb: Double = 0.2, maxIter: Int = 10)
//  : GraphFrame = {
//
//    val gx0 = g.toGraphX
//
//    val vColsMap = g.vertexColumnMap
//    val eColsMap = g.edgeColumnMap
//
//
//    // Convert vertex attributes to nice case classes.
//    // Initialize each node with hubScore = 1 and authScore = 1
//    val gx1: Graph[VertexAttr, Row] = gx0.mapVertices { case (_, attr) =>
//      VertexAttr(attr.getLong(vColsMap("id")), attr.getInt(vColsMap("outDegree")), resetProb)
//    }
//
//    val extractEdgeAttr: (GXEdge[Row] => EdgeAttr) = { e =>
//      val src = e.attr.getLong(eColsMap("src"))
//      val dst = e.attr.getLong(eColsMap("dst"))
//      EdgeAttr(src, dst)
//    }
//
//    var gx: Graph[VertexAttr, EdgeAttr] = gx1.mapEdges(extractEdgeAttr)
//
//    for (iter <- Range(1, maxIter)) {
//
//      val msgs: VertexRDD[PageMsg] = gx.aggregateMessages(
//        ctx =>
//          ctx.sendToDst(PageMsg(ctx.srcAttr.pageScore / (math.max(ctx.srcAttr.outDegree, 1)))),
//        reducer)
//
//      // Update page rank scores of each node
//      gx = gx.outerJoinVertices(msgs) {
//        case (vID, vAttr, optMsg) => {
//          val msg = optMsg.getOrElse(PageMsg(0.0))
//          VertexAttr(vAttr.srcId, vAttr.outDegree, resetProb + (1.0 - resetProb) * msg.pageScore)
//        }
//      }
//      println("Iter ", iter)
//    }
//
//    // Convert back to GraphFrame with a new column "belief" for vertices DataFrame.
//    // Inorder to deal with disconnected components
//    val gxFinal: Graph[Double, Unit] = gx.mapVertices((_, attr) => attr.pageScore)
//      .mapEdges(_ => ())
//
//    //gxFinal.edges.foreach(println)
//    gxFinal.vertices.foreach(println)
//    GraphFrame.fromGraphX(g, gxFinal, vertexNames = Seq("pageRank"))
//  }
//  // With Syntactic sugar
//  private def inDegreesRDD:VertexRDD[Int] = {
//
//    def mapper(implicit ctx:EdgeContext) = ctx.sendToDst(1)
//    def reducer(a:Int,b:Int):Int = (a+b)
//
//    graph.aggregateMessages(ctx  =>
//      ( mapper(ctx),
//        reducer,
//        TripletFields.None)
//  }
//}