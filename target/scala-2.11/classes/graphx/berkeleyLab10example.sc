import org.apache.spark.SparkContext
import org.apache.spark.graphx.GraphLoader
import org.apache.spark.sql.SparkSession

// REFERENCE : https://bcourses.berkeley.edu/courses/1267848/pages/lab-11

object berkeleyLab10example extends Serializable {
  def main(args: Array[String]) = {
    val spark = SparkSession.builder().master("local[2]").getOrCreate().sparkContext
  }

  private def trylab10example(spark: SparkContext) = {
    val labshomeedges = "src/main/scala/graphx/edges.txt"
    val labshomeverts = "src/main/scala/graphx/verts.txt"

    val edgeGraph = GraphLoader.edgeListFile(spark, labshomeedges)
    // \\s+ - matches sequence of one or more whitespace characters
    val verts = spark.textFile(labshomeverts).map{l => val lineSplits = l.split("\\s+")
    val id = lineSplits(0).trim.toLong
    val data = lineSplits.slice(1,lineSplits.length).mkString(" ")
      (id,data)}

    val g = edgeGraph.outerJoinVertices(verts)({(vid, _, title) => title.getOrElse("xxxx")}).cache

    val graph = edgeGraph.outerJoinVertices(verts) {
      case(uid, deg, Some(attrList)) => attrList
      case(uid, deg, None) => Array.empty[String]
    }
    graph.triplets.foreach(println(_))
  }
}