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

scalaVersion := "2.11.12"

val sparkVersion = "2.3.0"

resolvers +=  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

// https://docs.scala-lang.org/overviews/jdk-compatibility/overview.html
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-mllib" % sparkVersion % "provided",
  "org.scala-lang" % "scala-reflect" % sparkVersion % "provided"
)

// https://mvnrepository.com/artifact/org.apache.spark/spark-ganglia-lgpl
libraryDependencies += "org.apache.spark" %% "spark-ganglia-lgpl" % sparkVersion
//
//libraryDependencies ++= Seq(
//  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
//)

libraryDependencies += "ml.sparkling" %% "sparkling-graph-examples" % "0.0.8-SNAPSHOT"
libraryDependencies += "ml.sparkling" %% "sparkling-graph-loaders" % "0.0.8-SNAPSHOT"
libraryDependencies += "ml.sparkling" %% "sparkling-graph-operators" % "0.0.8-SNAPSHOT"

// Exclude Scala from the assembly jar, because spark already includes it.
assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
