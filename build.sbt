//2019/06/02 会社の環境で動かなかったため一時的に解除してみる
//2019/08/10 復活
import com.typesafe.tools.mima.plugin.MimaPlugin._
import interplay.ScalaVersions._


lazy val runProcesses = taskKey[Unit]("A task that hard codes the values to `run`")
runProcesses := {
  val _ = (log/runMain in Compile).toTask(" jp.co.nri.nefs.tool.log.file.Processes" +
  " --searchdir D:\\tmp3 --outputdir D:\\tmp4").value
  println("Done!")
}
lazy val runRecreate = taskKey[Unit]("A task that hard codes the values to `run`")
runRecreate := {
  val _ = (log/runMain in Compile).toTask(" jp.co.nri.nefs.tool.log.analysis.Case2Table " +
    "--recreate").value
  println("Done!")
}

lazy val runLog2Case = taskKey[Unit]("A task that hard codes the values to `run`")
runLog2Case := {
  val _ = (log/runMain in Compile).toTask(" jp.co.nri.nefs.tool.log.analysis.Log2Case --searchdir D:\\tmp4 --outputdir D:\\tmp5").value
  println("Done!")
}


lazy val runCase2Table = taskKey[Unit]("A task that hard codes the values to `run`")
runCase2Table := {
  val _ = (log/runMain in Compile).toTask(" jp.co.nri.nefs.tool.log.analysis.Case2Table --inputdir D:\\tmp5").value
  println("Done!")
}

lazy val runIvyCacheManagement = taskKey[Unit]("A task that hard codes the values to `run`")
runIvyCacheManagement := {
  val _ = (transport/runMain in Compile).toTask(" jp.co.nri.nefs.tool.transport.IvyCacheManagement --inputdir D:\\Apl\\.ivy2\\cache").value
  println("Done!")
}

lazy val runBringin = taskKey[Unit]("A task that hard codes the values to `run`")
runBringin := {
  val _ = (transport/runMain in Compile).toTask(" jp.co.nri.nefs.tool.transport.Bringin " +
    "--cachedir D:\\Apl\\.ivy2\\cache --afterdate " + """ "2019/07/01 00:00:00" """ + " --outputdir D:\\tmp6").value
  println("Done!")
}

lazy val runCache2Local = taskKey[Unit]("A task that hard codes the values to `run`")
runCache2Local := {
  val _ = (transport/runMain in Compile).toTask(" jp.co.nri.nefs.tool.transport.Cache2Local " +
    "--cachedir D:\\Apl\\.ivy2\\cache --outputdir D:\\cache2Local").value
  println("Done!")
}


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
  //2019/06/02 会社の環境で動かなかったため一時的に解除してみる
  //.settings(mimaSettings)
  .settings(commonSettings: _*)

lazy val log = (project in file ("log"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Dependencies.log)

lazy val transport = (project in file ("transport"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Dependencies.transport)
  .settings(resolvers += "Sonatype OSS Snapshots" at "file:///C:/pleiades/workspace/M2/repository")
  .dependsOn(log)

playBuildRepoName in ThisBuild := "analysis"

val previousVersion: Option[String] = None

//2019/06/02 会社の環境で動かなかったため一時的に解除してみる
//2019/08/10 復活
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