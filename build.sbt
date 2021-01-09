//2019/06/02 会社の環境で動かなかったため一時的に解除してみる
//2019/08/10 復活
import com.typesafe.tools.mima.plugin.MimaPlugin._
import interplay.ScalaVersions._

/*
lazy val runLogAnalyzer = taskKey[Unit]("A task that hard codes the values to `run`")
runLogAnalyzer := {
  val _ = (log/runMain in Compile).toTask(" jp.co.nri.nefs.tool.log.analysis.LogAnalyzer").value
  println("Done!")
}
*/


/*lazy val runProcesses = taskKey[Unit]("A task that hard codes the values to `run`")
runProcesses := {
  val _ = (log/runMain in Compile).toTask(" jp.co.nri.nefs.tool.log.file.Processes" +
  " --searchdir D:\\tmp3 --outputdir D:\\tmp4").value
  println("Done!")
}*/
/*lazy val runRecreate = taskKey[Unit]("A task that hard codes the values to `run`")
runRecreate := {
  val _ = (log/runMain in Compile).toTask(" jp.co.nri.nefs.tool.log.analysis.Case2Table " +
    "--recreate").value
  println("Done!")
}*/

/*lazy val runLog2Case = taskKey[Unit]("A task that hard codes the values to `run`")
runLog2Case := {
  val _ = (log/runMain in Compile).toTask(" jp.co.nri.nefs.tool.log.analysis.Log2Case --searchdir D:\\tmp4 --outputdir D:\\tmp5").value
  println("Done!")
}*/

/*lazy val runExcel2Case = taskKey[Unit]("A task that hard codes the values to `run`")
runExcel2Case := {
  val _ = (log/runMain in Compile).toTask(" jp.co.nri.nefs.tool.log.analysis.Log2Case --excelFile" +
    " D:\\data\\WindowDetail.xlsx --outputdir D:\\case").value
  println("Done!")
}*/


/*lazy val runCase2Table = taskKey[Unit]("A task that hard codes the values to `run`")
runCase2Table := {
  val _ = (log/runMain in Compile).toTask(" jp.co.nri.nefs.tool.log.analysis.Case2Table --inputdir D:\\tmp5").value
  println("Done!")
}*/

/*lazy val runIvyCacheManagement = taskKey[Unit]("A task that hard codes the values to `run`")
runIvyCacheManagement := {
  val _ = (transport/runMain in Compile).toTask(" jp.co.nri.nefs.tool.transport.IvyCacheManagement --inputdir D:\\Apl\\.ivy2\\cache").value
  println("Done!")
}*/

/*lazy val runBringin = taskKey[Unit]("A task that hard codes the values to `run`")
runBringin := {
  val _ = (transport/runMain in Compile).toTask(" jp.co.nri.nefs.tool.transport.Bringin " +
    "--cachedir D:\\Apl\\.ivy2\\local --afterdate " + """ "2019/09/20 00:00:00" """ + " --outputdir D:\\20191113_持ち込み").value
  println("Done!")
}*/

/*lazy val runCache2Local = taskKey[Unit]("A task that hard codes the values to `run`")
runCache2Local := {
  val _ = (transport/runMain in Compile).toTask(" jp.co.nri.nefs.tool.transport.Cache2Local " +
    "--cachedir D:\\Apl\\.ivy2\\cache --outputdir D:\\cache2Local").value
  println("Done!")
}*/

/*lazy val runAntExecute = taskKey[Unit]("A task that hard codes the values to `run`")
runAntExecute := {
  val _ = (transport/runMain in Compile).toTask(" jp.co.nri.nefs.tool.transport.Cache2Local " +
    "--execdir D:\\cache2Local").value
  println("Done!")
}*/

/*lazy val runAntExecuteTest = taskKey[Unit]("A task that hard codes the values to `run`")
runAntExecuteTest := {
  val _ = (transport/runMain in Compile).toTask(" jp.co.nri.nefs.tool.transport.Cache2Local " +
    "--execfile D:\\cache2Local\\org.xerial.sbt_sbt-sonatype_2.0_1.0_2.12.xml").value
  println("Done!")
}*/

lazy val commonSettings = Seq(
  // Work around https://issues.scala-lang.org/browse/SI-9311
  scalacOptions ~= (_.filterNot(_ == "-Xfatal-warnings")),
  scalaVersion := scala212,
  crossScalaVersions := Seq(scala211, scala212),
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)

name := "analysis"

version := "0.1"

//fork := true

lazy val actor = (project in file("jp.co.nri.nefs.tool.actor"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Dependencies.actor_test)


lazy val `play-analytics` = (project in file("jp.co.nri.nefs.tool.analytics"))
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
  .settings(libraryDependencies ++= Dependencies.analytics)
  .dependsOn(`play-slick`, `model-client`, `model-common`, util, config)

lazy val `analyze-client` = (project in file("jp.co.nri.nefs.tool.analytics.analyze.client"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Dependencies.analyze_client)
  .dependsOn(`model-client`, `store-common`, config)

lazy val config = project in file("jp.co.nri.nefs.tool.analytics.config")

lazy val `collect-client` = (project in file("jp.co.nri.nefs.tool.analytics.collect.client"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Dependencies.collect_client)
  .dependsOn(`model-client`, util)

lazy val `common-property` = (project in file("jp.co.nri.nefs.tool.analytics.common.property"))
  .settings(commonSettings: _*)
  .dependsOn(util)

lazy val `model-common` = (project in file("jp.co.nri.nefs.tool.analytics.model.common"))
  .settings(commonSettings: _*)
  .dependsOn(`play-slick`)

lazy val `model-client` = (project in file("jp.co.nri.nefs.tool.analytics.model.client"))
  .settings(commonSettings: _*)
  .dependsOn(`play-slick`)

lazy val `sender-client` = (project in file("jp.co.nri.nefs.tool.analytics.sender.client"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Dependencies.sender_client)
  .dependsOn(`store-client` % "test -> test; compile -> compile")

lazy val `generator-client` = (project in file("jp.co.nri.nefs.tool.analytics.generator.client"))
  .settings(commonSettings: _*)
  .dependsOn(`play-slick`, `model-client`, `common-property`, `store-common`, `store-client`, config)

lazy val `store-common` = (project in file("jp.co.nri.nefs.tool.analytics.store.common"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Dependencies.store_common)
  .dependsOn(`play-slick`, `model-common`, config)

lazy val `store-client` = (project in file("jp.co.nri.nefs.tool.analytics.store.client"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Dependencies.store_client)
  .settings(
    version := "1.0.0",
    organization := "jp.co.nri.nefs.tool",
    assemblyJarName in assembly := s"${name.value}-${version.value}.jar"
  )
  .dependsOn(`play-slick`, `store-common`, `model-client`, `common-property`, util, config)

lazy val training = (project in file("jp.co.nri.nefs.tool.analytics.training"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Dependencies.training)

lazy val util = (project in file("jp.co.nri.nefs.tool.util"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Dependencies.util)
/*
  .settings(
    scalacOptions in (Compile, doc) ++=
      Opts.doc.sourceUrl(s"https://github.com/eifaookfue/analysis/tree/development/jp.co.nri.nefs.tool.util/src/main/scala/${€{TPL_OWNER}.}/€{TPL_NAME}.scala")
  )
*/

lazy val producer = (project in file("jp.co.nri.nefs.tool.producer"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Dependencies.producer)
  .dependsOn(util)

lazy val websocket = (project in file("co-jp-nri-nefs-tool-websocket"))
  .settings(
    libraryDependencies ++= Dependencies.websocket
  )

lazy val elp = (project in file("elp"))
  .settings(
    libraryDependencies ++= Dependencies.elp
  )

lazy val transfer = (project in file("jp.co.nri.nefs.tool.analytics.transfer"))
  .settings(
    version := "1.0.0",
    organization := "jp.co.nri.nefs.tool",
    assemblyJarName in assembly := s"${name.value}-${version.value}.jar",
    assemblyMergeStrategy in assembly := {
      case "application.conf" => MergeStrategy.discard
      case "reference.conf" => MergeStrategy.discard
      case "logback.xml" => MergeStrategy.discard
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    }
  )
  .settings(libraryDependencies ++= Dependencies.transfer)
  .dependsOn(util)

lazy val json = (project in file("json"))
  .settings(libraryDependencies ++= Dependencies.json)


// バッククォートでくくると-も使用できる？
/*lazy val `play-slick` = (project in file("play-slick") )
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
  .dependsOn(log)*//*lazy val `play-slick` = (project in file("play-slick") )
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
  .dependsOn(log)*/

lazy val `play-slick` =(project in file("play.api.db.slick"))
  //.enablePlugins(PlayLibrary, Playdoc)
  .enablePlugins(PlayLibrary)
  .settings(libraryDependencies ++= Dependencies.play_slick)
  //2019/06/02 会社の環境で動かなかったため一時的に解除してみる
  //.settings(mimaSettings)
  .settings(commonSettings: _*)

lazy val transport = (project in file ("transport"))
  .settings(commonSettings: _*).settings(
    libraryDependencies ++= Dependencies.transport,
    resolvers += "Sonatype OSS Snapshots" at "file:///C:/pleiades/workspace/M2/repository",
    version := "1.0.0",
    organization := "jp.co.nri.nefs.tool",
    assemblyJarName in assembly := s"${name.value}-${version.value}.jar"
  )
  .dependsOn(util)

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