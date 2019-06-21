import com.typesafe.tools.mima.plugin.MimaPlugin._
import interplay.ScalaVersions._

lazy val commonSettings = Seq(
  // Work around https://issues.scala-lang.org/browse/SI-9311
  scalacOptions ~= (_.filterNot(_ == "-Xfatal-warnings")),
  scalaVersion := scala212,
  //scalaVersion := "2.12.8",
  crossScalaVersions := Seq(scala211, scala212)
  //crossScalaVersions := Seq("2.10.7", "2.12.8")
)

name := "analysis"

version := "0.1"

// バッククォートでくくると-も使用できる？
lazy val `play-slick` = (project in file("play-slick") )
  .enablePlugins(PlayScala)
  .disablePlugins(PlayFilters)
  .settings(
    libraryDependencies ++= Seq(
      Library.playSpecs2 % "test",
      // This could be removed after releasing https://github.com/playframework/playframework/pull/7266
      "org.fluentlenium" % "fluentlenium-core" % "3.2.0"
    ),
    concurrentRestrictions in Global += Tags.limit(Tags.Test, 1)
  ).settings(libraryDependencies += Library.h2)
  .settings(javaOptions in Test += "-Dslick.dbs.default.connectionTimeout=30 seconds")
  .settings(commonSettings: _*)
  .dependsOn(core)

lazy val core =(project in file("src/core"))
  //.enablePlugins(PlayLibrary, Playdoc)
  .enablePlugins(PlayLibrary)
  .settings(libraryDependencies ++= Dependencies.core)
  .settings(mimaSettings)
  .settings(commonSettings: _*)

lazy val apllog = (project in file("src/apllog"))
  .dependsOn(`play-slick`)
  .settings(libraryDependencies ++= Dependencies.apllog)
  .settings(assemblyJarName in assembly := "apllog.jar")
  .settings(mainClass in assembly := Some("jp.co.nri.nefs.tool.apllog.ZipUtils"))

playBuildRepoName in ThisBuild := "analysis"

// Binary compatibility is tested against this version
val previousVersion: Option[String] = None

def mimaSettings = mimaDefaultSettings ++ Seq(
  mimaPreviousArtifacts := Set(previousVersion flatMap { previousVersion =>
    if (crossPaths.value) Some(organization.value % s"${moduleName.value}_${scalaBinaryVersion.value}" % previousVersion)
    else Some(organization.value % moduleName.value % previousVersion)
  }).flatten
)

assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
  case PathList("org", "apache", "commons", "logging", "impl", xs @ _*)             => MergeStrategy.first
  case "application.conf"                            => MergeStrategy.concat
  case "unwanted.txt"                                => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}