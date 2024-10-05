ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.4"

lazy val root = (project in file("."))
    .settings(
        name := "cs441",
    )

name := "cs441"

lazy val app = (project in file("."))
    .settings(
        assembly / assemblyJarName := "MapReduceTokenizer.jar",
    )

libraryDependencies ++= Seq(
    "com.knuddels" % "jtokkit" % "1.1.0",
    "com.typesafe" % "config" % "1.4.2",
    "org.deeplearning4j" % "deeplearning4j-core" % "1.0.0-M1.1",
    "org.deeplearning4j" % "deeplearning4j-nlp" % "1.0.0-M1.1",
    "org.nd4j" % "nd4j-native-platform" % "1.0.0-M1.1",
    "org.apache.hadoop" % "hadoop-yarn-client" % "3.3.6",
    "org.apache.hadoop" % "hadoop-common" % "3.3.6",
    "org.apache.hadoop" % "hadoop-mapreduce-client-core" % "3.3.6",
    "org.apache.hadoop" % "hadoop-client" % "3.3.6",
    "org.apache.hadoop" % "hadoop-hdfs" % "3.3.6" % Test,
    "org.apache.hadoop" % "hadoop-hdfs-client" % "3.3.6",
    "org.apache.hadoop" % "hadoop-mapreduce-client-jobclient" % "3.3.6"
)

ThisBuild / assemblyMergeStrategy := {
    case x if Assembly.isConfigFile(x) =>
        MergeStrategy.concat
    case PathList(ps @ _*) if Assembly.isReadme(ps.last) || Assembly.isLicenseFile(ps.last) =>
        MergeStrategy.rename
    case PathList("META-INF", "services", "org.apache.hadoop.fs.FileSystem") =>
        MergeStrategy.filterDistinctLines
    case PathList("META-INF", xs @ _*) =>

        (xs map {_.toLowerCase}) match {
            case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) =>
                MergeStrategy.discard
            case ps @ (x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
                MergeStrategy.discard
            case "plexus" :: xs =>
                MergeStrategy.discard
            case "services" :: xs =>
                MergeStrategy.filterDistinctLines
            case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) =>
                MergeStrategy.filterDistinctLines
            case _ => MergeStrategy.first
        }
    case _ => MergeStrategy.first
}