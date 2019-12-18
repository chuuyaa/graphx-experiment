//package graphx.clustering
//
//import java.util
//
//import org.apache.spark.SparkContext
//import java.util.ArrayList
//
//import org.apache.spark.rdd.RDD
//
///**
//  * Refactored implementation of constraints cop kmeans clustering
//  * consists of run_ckm.py and cop_kmeans.py
//  */
//class Constraints (private var spark: SparkContext,
//                   private var datafile: Any,
//                   private var constraintsfile: Any,
//                   private var n_rep: Int,
//                   private var max_iter: Int,
//                   private var tolerance: Long) extends Serializable {
//
//  /**
//    * constract the class Constraints instance with default value of parameters
//    * @return
//    */
////  def this() = this(datafile=data, constraintsfile=constraintsfile, n_rep=5, max_iter=50, tolerance=1e-4)
//
//  /**
//    *
//    * @param spark
//    * @param datafile
//    * @return An RDD[(Long,Long)] of src and dst id
//    */
//  def read_data(spark: SparkContext, datafile: String)={
//    val f = spark.textFile(datafile).map{l => val lineSplits = l.split("\\s+")
//      val userid = lineSplits(0).trim.toLong
//      val username = lineSplits(1).trim
//      (userid,username)}
//    f.foreach(_.toString().toSeq)
//  }
//
//  /**
//    * to call :  val(ml,cl) = read_constraints(spark, path_small)
//    * @param spark
//    * @param constraintsfile
//    * @return List of ml and cl
//    */
//  def read_constraints(spark: SparkContext, constraintsfile: String)={
//    val f = spark.textFile(constraintsfile).map{l => val lineSplits = l.split("\\s+")
//      val src = lineSplits(0).trim.toLong
//      val dst = lineSplits(1).trim.toLong
//      val label = lineSplits(2).trim.toDouble
//      (src,dst, label)}
//    f
////    val ml = f.filter(x => x.toString.contains(",1")).collect.toList.map(x => (x._1, x._2))
////    val cl = f.filter(x => x.toString.contains(",-1")).collect.toList
////    (ml,cl)
//  }
//
//  def distance_12(point1: Long, point2: Long)={
//  }
//
//  def tolerance(tolerance: Long, dataset: List[Any])={
//  }
//
//  def closest_clusters(centers:Int, datapoint:Long)={
//  }
//
//  def initialize_centers(dataset: List[Any], k: Int, method: Any)={
//  }
//
//  def violate_constraints()={}
//
//  def compute_center()={}
//
//  def get_ml_info()={}
//
//  def transitive_closure(ml: List[(Long, Long, Int)], cl: List[(Long, Long, Int)], dataset_length: Int)={
//
//  }
//
//  def constraints_cluster(datafile: RDD[Any], k: Int, ml: List[(Long, Long, Int)], cl: List[(Long, Long, Int)], initialization: Any, max_iter: Int, tolerance: Long)={
//    val datafile_length = datafile.count().toInt
////    ml, cl = transitive_closure(ml, cl, datafile_length)
//  }
//
//  def run(spark: SparkContext, datafile: String, constraintsfile: String, k: Int, n_rep: Int, max_iter: Int, tolerance: Long)={
//    val data = read_data(spark, datafile)
//    val(ml,cl) = read_constraints(spark, constraintsfile)
//
//    val best_clusters = " "
//    val best_score = " "
//
//    for(x <- n_rep){
////      val clusters, centers = constraints_cluster(data, k, ml, cl, "degree", max_iter, tolerance)
//    }
//  }
//}
//
//
//
////https://stackoverflow.com/questions/6833501/efficient-iteration-with-index-in-scala