name := "CS441"

version := "0.1"

scalaVersion := "2.13.3"

fork := true

ThisBuild / useCoursier := true

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
javacOptions in (Compile, compile) ++= Seq("-source", "1.8", "-target", "1.8", "-g:lines")
mainClass in (Compile,run) := Some("Test")  //specifying fully qualified path of main c

libraryDependencies ++= Seq(
  //xml
  "org.scala-lang.modules" %% "scala-xml" % "1.3.0",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided,
  //Typesafe configuration
  "com.typesafe" % "config" % "1.4.0",
  // Logback logging framework
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "org.gnieh" % "logback-config" % "0.3.1",
  // JUnit Testing Framework
  "junit" % "junit" % "4.13",
  // Hadoop
  "org.apache.hadoop" % "hadoop-core" % "1.2.1",
  "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "3.3.0",
  "org.apache.hadoop" % "hadoop-client" % "2.2.0",
  "org.apache.mahout" %"mahout-integration" % "0.13.0"
)

