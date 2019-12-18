//package graphx
//import java.text.SimpleDateFormat
//import java.util.Calendar
//
//import graphx.indices.OperatorsNew._
//import org.apache.log4j.{Level, Logger}
//import org.apache.spark.SparkContext
//import org.apache.spark.graphx.{Graph, GraphLoader}
//import org.apache.spark.mllib.clustering._
//import org.apache.spark.sql.SparkSession
//
//
//object sparklingGraphTwitter extends Serializable {
//
//
//  def main(args: Array[String]): Unit = {
//    // Set log file to only see print errors
//    Logger.getLogger("org").setLevel(Level.ERROR)
//
//    // Spark entry point
//    val spark = SparkSession.builder().master("local[*]").config("spark.sql.warehouse.dir", "file:///Users/User/IdeaProject/graphx-experiment/").getOrCreate().sparkContext
//
//    //    val sqlContext = new org.apache.spark.sql.SQLContext(spark)
//    //    import sqlContext.implicits._
//
//    graphxProcessVOne(spark)
//    // calling garbage collector
//    System.gc()
//
//  }
//
//  private def graphxProcessVOne(spark: SparkContext)={
//
//    // for DataFrame conversion
//    // for datetime on output pathfile
//    val time = System.currentTimeMillis()
//    val formatter = new SimpleDateFormat("ddMMyyyy[hhmmss]_")
//
//    val calendar = Calendar.getInstance()
//    calendar.setTimeInMillis(time)
//    val date_time = formatter.format(calendar.getTime())
//
//    /**
//      * -------------------------------------------------------GRAPH GENERATION-------------------------------------------------------------------------
//      */
//    /**
//      * Load my user data and parse into tuples of user id and attribute list
//      */
//    val users = spark.textFile("/Users/User/IdeaProject/graphx-experiment/input/users.txt").map{l => val lineSplits = l.split("\\s+")
//      val id = lineSplits(0).trim.toLong
//      val data = lineSplits.slice(1,lineSplits.length).mkString(" ")
//      (id,data)}
//
//    /**
//      * Parse the edge data which is already in userId -> userId format
//      */
//    val relationships = GraphLoader.edgeListFile(spark, "/Users/User/IdeaProject/graphx-experiment/input/twitter.txt")
//
//    /**
//      * Attach the user attributes
//      */
//    val graph = relationships.outerJoinVertices(users){
//      case(uid, name, Some(attrList)) => attrList
//      // Some users may not have attributes so we set them as empty
//      case(uid, name, None) => "Missing data"
//    }
//
//
//    /**
//      * ((566386538, name1),(307458983, name2),1)
//      * ((srcId, srcAttr),(dstId, dstAttr), edgeAttr) edge attriv=bute by default is 1
//      */
//    //    println("GRAPH TRIPLETS :")
//    //    graph.triplets.foreach(println(_))
//
//    //try saved the output to a JSON file
//    //    val tripletsDataFrame = graph.triplets.map(triplet => (triplet.srcId, triplet.srcAttr, triplet.dstId, triplet.dstAttr, triplet.attr))
//    //      .toDF("srcId", "srcAttr", "dstId", "dstAttr", "edgeAttr")
//    //    tripletsDataFrame.write.json("/Users/User/IdeaProject/graphx-experiment/output/"+ date_time +"graphOutputJSON.txt")
//
//    //graph.triplets.saveAsTextFile("testOutV.txt")
//
//    /**
//      * Compute the PageRank
//      * Output :
//      * ((11330027,1.7562175998195302),(17224642,6.040126131542945),0.00847457627118644)
//      * ((568770231,0.22816618569378982),(113751683,2.2956495665192036),0.06666666666666667)
//      * Explanation:
//      * ((srcID, srcAttr),(dstId, dstAttr), edgeAttr))
//      */
//
//    val pagerankGraph = graph.pageRank(0.001)
//    val ranks = graph.pageRank(0.0001).vertices
//
//    //    println("PAGERANK GRAPH TRIPLETS :")
//    //    pagerankGraph.triplets.foreach(println(_))
//
//    /**
//      * Get the attributes of the top pagerank users
//      */
//    //    val userInfoWithPageRank = graph.outerJoinVertices(pagerankGraph.vertices) {
//    //      case (uid, attrList, Some(pr)) => (pr, attrList)
//    //      case (uid, attrList, None) => (0.0, attrList)
//    //    }
//
//    /**
//      * -------------------------------------------------------SIMILARITY INDEX COMPUTATION------------------------------------------------------------------
//      */
//    /**
//      * INDEX 1 : COMMON NEIGHBOURS
//      * FORMULA : intersect (neighbours1, neighbours2)
//      */
//    // Graph where each edge is associated with number of common neighbours of vertices on edge
//    val commonNeighbours: Graph[_, Int] = pagerankGraph.commonNeighbours()
//    //    val cnEdges = commonNeighbours.edges
//
//
//    //    val CNtripletsDF = commonNeighbours.triplets.map(triplet => (triplet.srcId, triplet.srcAttr, triplet.dstId, triplet.dstAttr, triplet.attr))
//    //      .toDF("srcId", "srcAttr", "dstId", "dstAttr", "edgeAttr")
//    //    CNtripletsDF.write.json("CNOutputJSON.txt")
//
//    //    commonNeighbours.triplets.saveAsTextFile("/Users/User/IdeaProject/graphx-experiment/output/"+ date_time +"CNOut.txt")
//    /**
//      * INDEX 2 : ADAMIC ADAR
//      * FORMULA : 1 / log (degree(common neighbours))
//      * OUTPUT :
//      * .triplets
//      * ((4,{7=>{4}, 8=>{4, 6}, 3=>{2, 4}, 5=>{2, 4, 6}, 6=>{5, 4, 8}, 2=>{5, 4, 3, 1}, 1=>{2, 4}}),(8,{4=>{7, 8, 3, 5, 6, 2, 1}, 6=>{5, 4, 8}}),0.9102392266268373)
//      */
//    // Graph where each edge is associated with its Adamic Adar measure
//    val adamicAdarGraph: Graph[_, Double] = pagerankGraph.adamicAdar(true)
//    val aaEdges = adamicAdarGraph.edges
//
//
//    //    val AAtripletsDF = adamicAdarGraph.triplets.map(triplet => (triplet.srcId, triplet.srcAttr, triplet.dstId, triplet.dstAttr, triplet.attr))
//    //      .toDF("srcId", "srcAttr", "dstId", "dstAttr", "edgeAttr")
//    //    AAtripletsDF.write.json("AAOutputJSON.txt")
//
//    //    adamicAdarGraph.edges.saveAsTextFile("/Users/User/IdeaProject/graphx-experiment/output/"+ date_time +"AAOut.txt")
//
//    /**
//      * INDEX 3 : RESOURCE ALLOCATION
//      * FORMULA :
//      */
//    // Graph where each edge is associated with its Adamic Adar measure
//    val resourceAllocationGraph: Graph[_, Double] = pagerankGraph.resourceAllocation(true)
//    //    val raEdges = resourceAllocationGraph.edges
//
//    //    val RAtripletsDF = resourceAllocationGraph.triplets.map(triplet => (triplet.srcId, triplet.srcAttr, triplet.dstId, triplet.dstAttr, triplet.attr))
//    //      .toDF("srcId", "srcAttr", "dstId", "dstAttr", "edgeAttr")
//    //    RAtripletsDF.write.json("RAOutputJSON.txt")
//
//    //    resourceAllocationGraph.edges.saveAsTextFile("/Users/User/IdeaProject/graphx-experiment/output/"+ date_time +"RAOut.txt")
//
//    /**
//      * INDEX 4 : PREFERENTIAL ATTACHMENT
//      * FORMULA :
//      */
//    // Graph where each edge is associated with its Adamic Adar measure
//    val preferentialAttacmentGraph: Graph[_, Int] = pagerankGraph.preferentialAttachment(true)
//    //    val paEdges = preferentialAttacmentGraph.edges
//
//    //    val PAtripletsDF = preferentialAttacmentGraph.triplets.map(triplet => (triplet.srcId, triplet.srcAttr, triplet.dstId, triplet.dstAttr, triplet.attr))
//    //      .toDF("srcId", "srcAttr", "dstId", "dstAttr", "edgeAttr")
//    //    PAtripletsDF.write.json("PAOutputJSON.txt")
//
//    //    preferentialAttacmentGraph.edges.saveAsTextFile("/Users/User/IdeaProject/graphx-experiment/output/"+ date_time +"PAOut.txt")
//
//    /**
//      * INDEX 5 : JACCARD INDEX
//      * FORMULA :
//      */
//    // Graph where each edge is associated with its Adamic Adar measure
//    val jaccardIndexGraph: Graph[_, Double] = pagerankGraph.jaccardIndex(true)
//    //    val jiEdges = jaccardIndexGraph.edges
//
//
//    //    jaccardIndexGraph.triplets.saveAsTextFile("/Users/User/IdeaProject/graphx-experiment/output/"+ date_time +"JIOut.txt")
//
//    /**
//      * print out all index computed
//      */
//    //    println("COMMON NEIGHBOURS TRIPLETS :")
//    //    commonNeighbours.edges.foreach(println(_))
//    //
//    //    println("ADAMIC ADAR TRIPLETS :")
//    //    adamicAdarGraph.edges.foreach(println(_))
//    //
//    //    println("RESOURCE ALLOCATION TRIPLETS :")
//    //    resourceAllocationGraph.edges.foreach(println(_))
//    //
//    //    println("PREFERENTIAL ATTACHMENT TRIPLETS :")
//    //    preferentialAttacmentGraph.edges.foreach(println(_))
//    //
//    //    println("JACCARD INDEX TRIPLETS :")
//    //    jaccardIndexGraph.edges.foreach(println(_))
//    //
//    /**
//      * --------------------------------------------------UPDATE ALL GRAPH PROPERTY INTO COMBINED EDGES-----------------------------------------------------
//      */
//    //    var joinedEdges = cnEdges.innerJoin(aaEdges){
//    //      case (srcId, dstId, graph1Attr, graph2Attr) => graph1Attr.toDouble
//    //    }
//    //
//    //    joinedEdges = joinedEdges.innerJoin(raEdges) {
//    //      case (srcId, dstId, graph1Attr, graph2Attr) => graph1Attr.toDouble
//    //    }
//    //
//    //    joinedEdges = joinedEdges.innerJoin(paEdges){
//    //      case (srcId,dstId, graph1Attr, graph2Attr) => graph1Attr.toDouble
//    //    }
//    //
//    //    joinedEdges = joinedEdges.innerJoin(jiEdges) {
//    //      case (srcId, dstId, graph1Attr, graph2Attr) => graph1Attr.toDouble
//    //    }
//    //    println("combined edges")
//    //    val collectedEdges = joinedEdges.collect().foreach(println(_))
//
//    /**
//      * ---------------------------------------------------TRAIN TEST SPLITS GRAPH-----------------------------------------------------------------------
//      */
//
//
//    //    val validGraph = pagerankGraph.subgraph(vpred = (id, graphattr) => graphattr._2 != "8")
//
//    // Split data into training (60%) and test (40%).
//    //    val splits = data.randomSplit(Array(0.6, 0.4), seed = 11L)
//    //    val training = splits(0).cache()
//    //    val test = splits(1)
//
//    //    val splits = pagerankGraph.edges.randomSplit(Array(0.6, 0.4),11L)
//    //    val training = splits(0).cache()
//    //    val test = splits(1)
//    //
//    //    // Run training algorithm to build the model
//    //    val numIterations = 100
//    //    val model = SVMWithSGD.train(training,numIterations)
//    //      .train(training, numIterations)
//    //
//    //    // Clear the default threshold.
//    //    model.clearThreshold()
//    //
//    //    // Compute raw scores on the test set.
//    //    val scoreAndLabels = test.map { point =>
//    //      val score = model.predict(point.features)
//    //      (score, point.label)
//    //    }
//    //
//    //    // Get evaluation metrics.
//    //    val metrics = new BinaryClassificationMetrics(scoreAndLabels)
//    //    val auROC = metrics.areaUnderROC()
//    //
//    //    println("Area under ROC = " + auROC)
//    //
//    //    println(training)
//    //    println(test)
//
//
//
//    //    val Array(trainingGraph, testGraph) = joinedEdges.randomSplit(Array(0.6,0.4))
//    //    println("trainingGraph")
//    //    trainingGraph.foreach(println(_))
//    //    println("testGraph")
//    //    testGraph.foreach(println(_))
//
//    //
//    //    // Basic model
//    //    val lr = new LogisticRegression().setMaxIter(200).setRegParam(0.01).setElasticNetParam(0.8)
//    //
//    //    // Train
//    //    val lrModel = lr.fit(adamicAdarGraph.triplets.toDF())
//    //    println(s"Coefficients: ${lrModel.coefficients} Intercept: ${lrModel.intercept}")
//    //
//    //    // Summary & ROC
//    //    val trainingSummary = lrModel.summary
//    //    val binarySummary = trainingSummary.asInstanceOf[BinaryLogisticRegressionSummary]
//    //    println(s"areaUnderROC: ${binarySummary.areaUnderROC}")
//    //
//    //    // F scores & treshold
//    //    val fMeasure = binarySummary.fMeasureByThreshold
//    //    val maxFMeasure = fMeasure.select(max("F-Measure")).head().getDouble(0)
//    //    val bestThreshold = fMeasure.where($"F-Measure" === maxFMeasure).select("threshold").head().getDouble(0)
//    //    lrModel.setThreshold(bestThreshold)
//    //    println(s"max F-measure: ${maxFMeasure}")
//    //    println(s"best threshold: ${bestThreshold}")
//    //
//    //    // test model
//    //    val predictions = lrModel.transform(testData)
//    //
//    //    // evaluate test
//    //    val evaluator = new BinaryClassificationEvaluator()
//    //    val accuracy = evaluator.evaluate(predictions)
//    //    println("Test Error = " + (1.0 - accuracy))
//    //
//    //    // save model
//    //    lrModel.write.overwrite().save(Config.lrModelFile)
//    //    println("Saved model " + Config.lrModelFile)
//
//
//
//
//    //    val combinedGraphs = Graph(users,joinedEdges)
//    //
//    //
//    //    combinedGraphs.cache()
//    //    combinedGraphs.triplets.foreach(println(_))
//
//    //        //Cluster the data into two classes using KMeans.
//    //        val numIterations = 0
//    //        val clusters = KMeans.train(combinedGraphs, 2, 20)
//    //        // Compute the sum of squared errors.
//    //        val cost = clusters.computeCost(parsedData)
//    //        println("Sum of squared errors = " + cost)
//
//
//    val aaCombinedGraph = Graph(pagerankGraph.vertices, aaEdges)
//    aaCombinedGraph.triplets.foreach(println(_))
//
//    val pagerankModel  =
//      new PowerIterationClustering()
//        .setK(3)
//        .setMaxIterations(15)
//        .setInitializationMode("degree")
//        .run(pagerankGraph)
//    val adamicadarModel  =
//      new PowerIterationClustering()
//        .setK(3)
//        .setMaxIterations(15)
//        .setInitializationMode("degree")
//        .run(aaCombinedGraph)
//    //
//    val pagerankClusters = pagerankModel.assignments.collect().groupBy(_.cluster).mapValues(_.map(_.id))
//    //    pagerankClusters.foreach(println(_))
//    val assignments = pagerankClusters.toList.sortBy { case (k, v) => v.length}
//    val assignmentsStr = assignments
//      .map { case (k,v) =>
//        s"$k -> ${v.sorted.mkString("[", ",", "]")}"
//      }.mkString(", ")
//    val sizesStr = assignments.map {
//      _._2.length
//    }.sorted.mkString("(", ",", ")")
//    println(s"pagerank cluster sizes: $sizesStr")
//    //
//    val adamicadarClusters = adamicadarModel.assignments.collect().groupBy(_.cluster).mapValues(_.map(_.id))
//    //    adamicadarClusters.foreach(println(_))
//    val assignments2 = adamicadarClusters.toList.sortBy{ case (k,v) => v.length}
//    //    val assignments2Str = assignments2
//    //      .map { case (k,v) =>
//    //        s"$k -> ${v.sorted.mkString("[", ",", "]")}"
//    //      }.mkString(", ")
//    val sizes2Str = assignments2.map {
//      _._2.length
//    }.sorted.mkString("(", ",", ")")
//    println(s"adamic adar cluster sizes: $sizes2Str")
//
//
//    val test = new convertGraph(graph)
//    println(test.printAdjacencyList)
//    test.printAdjacencyList.foreach(println(_))
//    //    test.printAdjacencyMatrix
//    //test.printEdges
//  }
//
//  class convertGraph(val graphc:Graph[_,_]){
//    def size: Int = graphc.vertices.count().toInt
//    val adjacent = graphc.edges groupBy(_.srcId) mapValues(_ map(_.dstId))
//    //    def adjacencyMatrix = adjacent mapValues(_.toSet) mapValues(0 to size)
//    //
//    //
//    def printAdjacencyList = adjacent mapValues (_ mkString ",")
//    //    def printAdjacencyMatrix = adjacencyMatrix mapValues(_ mkString ", ")
//  }
//}
