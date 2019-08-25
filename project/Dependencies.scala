import sbt._

  object Dependencies {
    val core = Seq(
      Library.slick,
      Library.slickHikariCP,
      Library.playJdbcApi,
      Library.mysql,
      Library.playLogback % "test",
      Library.playSpecs2 % "test",
      Library.h2 % "test"
    )

    val log = Seq(
      Library.slick,
      Library.slickHikariCP,
      Library.h2 % "test",
      Library.mysql
    )

    val transport = Seq(
      Library.nefs_common_util,
      Library.logback
    )
  }

  object Version {
    //val play = _root_.play.core.PlayVersion.current
    val play         = "2.7.0"
    val slick        = "3.3.0"
    val h2           = "1.4.197"
    val scalaLogging = "3.9.2"
    val mysql        = "8.0.15"
    val nefs_common_util = "1.0.0-SNAPSHOT"
    val logback      = "1.0.13"
  }

  object Library {
    val playLogback         = "com.typesafe.play"          %% "play-logback"             % Version.play
    val playJdbcApi         = "com.typesafe.play"          %% "play-jdbc-api"            % Version.play
    val playJdbcEvolutions  = "com.typesafe.play"          %% "play-jdbc-evolutions"     % Version.play
    val playSpecs2          = "com.typesafe.play"          %% "play-specs2"              % Version.play
    val slick               = "com.typesafe.slick"         %% "slick"                    % Version.slick
    val slickHikariCP       = "com.typesafe.slick"         %% "slick-hikaricp"           % Version.slick
    val h2                  = "com.h2database"             %  "h2"                       % Version.h2
    val sacalaLogging       = "com.typesafe.scala-logging" %% "scala-logging"            % Version.scalaLogging
    val mysql               = "mysql"                      % "mysql-connector-java"      % Version.mysql
    val nefs_common_util    = "jp.co.nri.nefs.common"      % "nefs-common-util"          % Version.nefs_common_util
    val logback             = "ch.qos.logback"             % "logback-core"              % Version.logback
  }

