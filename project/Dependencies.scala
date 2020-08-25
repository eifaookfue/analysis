import sbt._

object Dependencies {
  
  val actor_test: Seq[ModuleID] = Seq(
    Library.akka_actor,
    Library.akka_slf4j,
    Library.scalaLogging,
    Library.logbackClassic
  )

  val analytics: Seq[ModuleID] = Seq(
    Library.scalaLogging,
    Library.logbackClassic,
    Library.config
  )
  
  val analyze_client: Seq[ModuleID] = Seq(
    Library.scalaLogging,
    Library.logbackClassic,
    Library.config
  )
  
  val producer: Seq[ModuleID] = Seq(
    Library.config,
    Library.common_util,
    Library.oms_order_service,
    Library.scalaLogging,
    Library.logbackClassic,
    Library.oms_entity_property,
    Library.order_service_proxy,
    Library.common_model,
    Library.common_di
  )
  
  val sender_client: Seq[ModuleID] = Seq(
    Library.akka_actor,
    Library.akka_slf4j
  )
  
  val store_client: Seq[ModuleID] = Seq(
    Library.scalaLogging,
    Library.logbackClassic,
    Library.config,
    Library.guice,
    Library.h2 % "test"
  )
  
  val store_common: Seq[ModuleID] = Seq(
    Library.guice,
    Library.config,
    Library.scalaLogging,
    Library.logbackClassic
  )

  val util: Seq[ModuleID] = Seq(
    Library.scalaLogging,
    Library.logbackClassic,
    Library.poi,
    Library.poi_ooxml,
    Library.config
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

  val collect_client: Seq[ModuleID] = Seq(
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

  val play_slick: Seq[ModuleID] = Seq(
    Library.slick,
    Library.slickHikariCP,
    Library.playJdbcApi,
    Library.mysql,
    Library.guice,
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

  val training: Seq[ModuleID] = Seq(
    Library.scala_xml
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
  val scala_xml    = "1.1.0"
  val common_util  = "1.0.0"
  val oms_entity_property = "1.0.0"
  val oms_order_service = "1.0.0"
  val order_service_proxy = "1.0.0"
  val common_model = "1.0.0"
  val common_di    = "1.0.0"
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
  val akka_slf4j          = "com.typesafe.akka"          %% "akka-slf4j"               % Version.akka_actor
  val scalaz              = "org.scalaz"                 %% "scalaz-core"              % Version.scalaz
  val guice               = "com.google.inject"          % "guice"                     % Version.guice
  val scala_xml           = "org.scala-lang.modules"     %% "scala-xml"                % Version.scala_xml
  val common_util         = "jp.co.nri.nefs.common"      % "nefs-common-util"          % Version.common_util
  val oms_entity_property = "jp.co.nri.nefs.oms"         % "nefs-oms-entity-property"  % Version.oms_entity_property
  val oms_order_service   = "jp.co.nri.nefs.oms"         % "nefs-oms-order-serivce-entity-property" % Version.oms_order_service
  val order_service_proxy = "jp.co.nri.nefs.oms"         % "nefs-oms-order-serivce-proxy" % Version.order_service_proxy
  val common_model        = "jp.co.nri.nefs.common"      % "nefs-common-model"         % Version.common_model
  val common_di           = "jp.co.nri.nefs.common"      % "nefs-common-di"            % Version.common_di
}

