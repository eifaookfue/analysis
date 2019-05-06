import sbt._

  object Dependencies {
    val core = Seq(
      Library.slick,
      Library.slickHikariCP,
      Library.playJdbcApi,
      Library.playLogback % "test",
      Library.playSpecs2 % "test",
      Library.h2 % "test"
    )

    val apllog = Seq(
      Library.slick,
      Library.slickHikariCP,
      Library.h2 % "test",
      Library.sacalaLogging
      /*libraryDependencies ++= Seq (
        "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
        "com.typesafe.slick" %% "slick" % "3.3.0",
        "com.typesafe.slick" %% "slick-hikaricp" % "3.3.0",
        "com.h2database" % "h2" % "1.4.191",
        "mysql" % "mysql-connector-java" % "8.0.15"
      )*/
    )
  }

  object Version {
    //val play = _root_.play.core.PlayVersion.current
    val play         = "2.7.0"
    val slick        = "3.3.0"
    val h2           = "1.4.197"
    val scalaLogging = "3.9.2"
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
  }

