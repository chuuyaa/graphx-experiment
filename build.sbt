import sun.security.tools.PathList

lazy val root = (project in file(".")).
  settings(
    name := "graphx-experiment",
    version := "1.0",
    scalaVersion := "2.11.0",
    mainClass in Compile := Some("graphx.sparklingGraphTwitter")
  )

name := "graphx-experiment"

version := "1.0"

scalaVersion := "2.11.0"

resolvers +=  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "org.apache.spark" %% "spark-core" % "2.3.0" % "provided",
  "org.apache.spark" %% "spark-sql" % "2.3.0" % "provided",
  "org.apache.spark" %% "spark-mllib" % "2.3.0" % "provided",
  "org.scala-lang" % "scala-reflect" % "2.3.0" % "provided"
)
//
//libraryDependencies ++= Seq(
//  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
//  "org.apache.spark" %% "spark-core" % "2.3.0",
//  "org.apache.spark" %% "spark-sql" % "2.3.0",
//  "org.apache.spark" %% "spark-mllib" % "2.3.0",
//  "org.scala-lang" % "scala-reflect" % "2.3.0"
//)

libraryDependencies += "ml.sparkling" %% "sparkling-graph-examples" % "0.0.7-SNAPSHOT"
libraryDependencies += "ml.sparkling" %% "sparkling-graph-loaders" % "0.0.7-SNAPSHOT"
libraryDependencies += "ml.sparkling" %% "sparkling-graph-operators" % "0.0.7-SNAPSHOT"

assemblyMergeStrategy in assembly := {
  case PathList("org","aopalliance", xs @ _*) => MergeStrategy.last
  case PathList("javax", "inject", xs @ _*) => MergeStrategy.last
  case PathList("javax", "servlet", xs @ _*) => MergeStrategy.last
  case PathList("javax", "activation", xs @ _*) => MergeStrategy.last
  case PathList("org", "apache", xs @ _*) => MergeStrategy.last
  case PathList("com", "google", xs @ _*) => MergeStrategy.last
  case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.last
  case PathList("com", "codahale", xs @ _*) => MergeStrategy.last
  case PathList("com", "yammer", xs @ _*) => MergeStrategy.last
  case "about.html" => MergeStrategy.rename
  case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
  case "META-INF/mailcap" => MergeStrategy.last
  case "META-INF/mimetypes.default" => MergeStrategy.last
  case "plugin.properties" => MergeStrategy.last
  case "log4j.properties" => MergeStrategy.last
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
