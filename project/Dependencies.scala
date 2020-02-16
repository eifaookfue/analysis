import sbt._

  object Dependencies {

    val analytics: Seq[ModuleID] = Seq(
      Library.scalaLogging,
      Library.logbackClassic,
      Library.config
    )

    val store: Seq[ModuleID] = Seq(
      Library.scalaLogging,
      Library.logbackClassic,
      Library.config,
      Library.akka_actor
    )

    val util: Seq[ModuleID] = Seq(
      Library.scalaLogging,
      Library.logbackClassic,
      Library.poi,
      Library.poi_ooxml
    )

    val websocket: Seq[ModuleID] = Seq(
      Library.json,
      Library.ws,
      Library.ws_json,
      Library.akka_http,
      Library.akka_stream,
      Library.scalaLogging,
      Library.logbackClassic,
      Library.config
    )
    
    val elp: Seq[ModuleID] = Seq(
      Library.json,
      Library.ws,
      Library.ws_json,
      Library.akka_http,
      Library.akka_stream,
      Library.scalaLogging,
      Library.logbackClassic,
      Library.config
    )

    val core: Seq[ModuleID] = Seq(
      Library.slick,
      Library.slickHikariCP,
      Library.playJdbcApi,
      Library.mysql,
      Library.playLogback % "test",
      Library.playSpecs2 % "test",
      Library.h2 % "test"
    )

    val transfer: Seq[ModuleID] = Seq(
      Library.config,
      Library.scalaLogging,
      Library.logbackClassic
    )

    val json: Seq[ModuleID] = Seq(
      Library.json,
      Library.ws,
      Library.ws_json
    )

    val log: Seq[ModuleID] = Seq(
      Library.slick,
      Library.slickHikariCP,
      Library.h2 % "test",
      Library.mysql,
      Library.poi,
      Library.poi_ooxml,
      Library.scalaLogging,
      Library.logbackClassic,
      Library.akka_actor,
      Library.scalaz,
      Library.guice
    )

    val transport: Seq[ModuleID] = Seq(
      Library.logback,
      Library.scalaLogging,
      Library.logbackClassic
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
    val poi          = "3.9"
    val poi_ooxml    = "3.9"
    val ws           = "2.1.0-RC2"
    val config       = "1.3.2"
    val akka_http    = "10.1.10"
    val akka_stream  = "2.5.23"
    val akka_actor  =  "2.5.19"
    val scalaz       = "7.2.14"
    val guice        = "4.2.2"
  }

  object Library {
    val playLogback         = "com.typesafe.play"          %% "play-logback"             % Version.play
    val playJdbcApi         = "com.typesafe.play"          %% "play-jdbc-api"            % Version.play
    val playJdbcEvolutions  = "com.typesafe.play"          %% "play-jdbc-evolutions"     % Version.play
    val playSpecs2          = "com.typesafe.play"          %% "play-specs2"              % Version.play
    val slick               = "com.typesafe.slick"         %% "slick"                    % Version.slick
    val slickHikariCP       = "com.typesafe.slick"         %% "slick-hikaricp"           % Version.slick
    val h2                  = "com.h2database"             %  "h2"                       % Version.h2
    val scalaLogging       = "com.typesafe.scala-logging" %% "scala-logging"            % Version.scalaLogging
    val mysql               = "mysql"                      % "mysql-connector-java"      % Version.mysql
    val logback             = "ch.qos.logback"             % "logback-core"              % Version.logback
    val logbackClassic      = "ch.qos.logback"             % "logback-classic"           % Version.logback
    val poi                 = "org.apache.poi"             % "poi"                       % Version.poi
    val poi_ooxml           = "org.apache.poi"             % "poi-ooxml"                 % Version.poi_ooxml
    val json                = "com.typesafe.play"          %% "play-json"                % Version.play
    val ws                  = "com.typesafe.play"          %% "play-ahc-ws-standalone"   % Version.ws
    val ws_json              = "com.typesafe.play"          %% "play-ws-standalone-json"  % Version.ws
    val config              = "com.typesafe"               %  "config"                   % Version.config
    val akka_http           = "com.typesafe.akka"          %% "akka-http"                % Version.akka_http
    val akka_stream         = "com.typesafe.akka"          %% "akka-stream"              % Version.akka_stream
    val akka_actor          = "com.typesafe.akka"          %% "akka-actor"               % Version.akka_actor
    val scalaz              = "org.scalaz"                 %% "scalaz-core"              % Version.scalaz
    val guice               = "com.google.inject"          % "guice"                     % Version.guice
  }

