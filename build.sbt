import sun.security.tools.PathList

lazy val root = (project in file(".")).
  settings(
    name := "graphx-experiment",
    version := "1.0",
    scalaVersion := "2.12.0",
    mainClass in Compile := Some("graphx.sparklingGraphTwitter")
  )

name := "graphx-experiment"

version := "1.0"

scalaVersion := "2.12.0"

resolvers +=  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "org.apache.spark" %% "spark-core" % "2.3.0" % "provided",
  "org.apache.spark" %% "spark-sql" % "2.3.0" % "provided",
  "org.apache.spark" %% "spark-mllib" % "2.3.0" % "provided",
  "org.scala-lang" % "scala-reflect" % "2.3.0" % "provided"
)

// https://mvnrepository.com/artifact/org.apache.spark/spark-ganglia-lgpl
libraryDependencies += "org.apache.spark" %% "spark-ganglia-lgpl" % "2.1.0"
//
//libraryDependencies ++= Seq(
//  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
//  "org.apache.spark" %% "spark-core" % "2.3.0",
//  "org.apache.spark" %% "spark-sql" % "2.3.0",
//  "org.apache.spark" %% "spark-mllib" % "2.3.0",
//  "org.scala-lang" % "scala-reflect" % "2.3.0"
//)

libraryDependencies += "ml.sparkling" %% "sparkling-graph-examples" % "0.0.8-SNAPSHOT"
libraryDependencies += "ml.sparkling" %% "sparkling-graph-loaders" % "0.0.8-SNAPSHOT"
libraryDependencies += "ml.sparkling" %% "sparkling-graph-operators" % "0.0.7-SNAPSHOT"
libraryDependencies += "ml.sparkling" %% "sparkling-graph-operators" % "0.0.8-SNAPSHOT"

