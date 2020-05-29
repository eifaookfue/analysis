package jp.co.nri.nefs.tool.analytics.training

import java.io.File
import java.nio.file.{Files, Paths, StandardOpenOption}

import scala.collection.JavaConverters._
import scala.xml.XML
import scala.language.implicitConversions

case class Station(name: String, passgrs: Int, latit: Double, longit: Double, raicoCo: String, line: String)

object Station {

  import RichSeq._

  implicit class CSVWrapper(val prod: Product) extends AnyVal {
    def toCSV: String = prod.productIterator.map {
      case Some(value) => value
      case None => ""
      case rest => rest
    }.mkString(",")
  }

  def main(args: Array[String]): Unit = {
    val str = "C:/Users/user/Documents/20200519_Presentation/S12-13/S12-13.xml"
    val file = new File(str)
    val s12Data = XML.loadFile(file)
    val latMap = (for {
      node <- s12Data \ "Curve"
      id = (node \ "@{http://www.opengis.net/gml/3.2}id").head.text
      //attrNode <- node.attribute("http://www.opengis.net/gml/3.2", "id")
      //id = attrNode.head.text
      raw = (node \\ "posList").text
      latit = Seq(2, 5).map(i => raw.split("[ ]+|\n")(i).toDouble).avg
      longit = Seq(3, 6).map(i => raw.split("[ ]+|\n")(i).toDouble).avg
    } yield (id, (latit, longit))).toMap
    latMap.takeRight(2).foreach(println)

    val curves = for {
      node <- s12Data \ "TheNumberofTheStationPassengersGettingonandoff"
      stationName = (node \ "stationName").head.text
      //if stationName == "あざみ野"
      passenger = (node \ "passengers2012").head.text.toInt
      railCo = (node \ "administrationCompany").head.text
      routeName = (node \ "routeName").head.text
      href = (node \ "station" \ "@{http://www.w3.org/1999/xlink}href").head.text.replace("#","")
    } yield (stationName, passenger, railCo, routeName, href)
    curves.takeRight(2).foreach(println)
    val stations = curves.groupBy(_._1).map { case (stationName, seq) =>
      val passenger = seq.map(_._2).sum
      val railCo = seq.map(_._3).last
      val routeName = seq.map(_._4).last
      val (latit, longit) = seq.map(_._5).map(latMap(_)).avg
      Station(stationName, passenger, latit, longit, railCo, routeName)
    }
    val (tokyoLatit, tokyoLongit) = stations.filter(_.name == "東京").map(s => (s.latit, s.longit)).head
    val strs = stations.map{station =>
      val diffLatit = scala.math.abs(station.latit - tokyoLatit)
      val diffLongit = scala.math.abs(station.longit - tokyoLongit)
      val diff = diffLatit + diffLongit
      (diff, station)
    }.toSeq.sortBy(_._1).take(869).sortBy(_._2.name).map(_._2.toCSV)
    val strs2 = for ((str, index) <- strs.zipWithIndex) yield (index + 1).toString + "," + str
    val out = Paths.get("C:/Users/user/Documents/20200519_Presentation/data/TokyoSTATION.csv")
    Files.write(out, Seq("StNo,Station,Passgrs.Latit.Longit,RailCo,Line").asJava, StandardOpenOption.TRUNCATE_EXISTING)
    Files.write(out, strs2.asJava, StandardOpenOption.APPEND)
  }
}

object RichSeq {
  implicit def seqToRichSeq(seq: Seq[Double]): RichSeq = new RichSeq(seq)
  implicit def seqToRichTupleSeq(seq: Seq[(Double, Double)]): RichTupleSeq = new RichTupleSeq(seq)
}

class RichSeq(seq: Seq[Double]) {
  def avg: Double = seq.sum / seq.length
}

class RichTupleSeq(seq: Seq[(Double, Double)]) {
  import RichSeq.seqToRichSeq
  def avg: (Double, Double) = (seq.map(_._1).avg, seq.map(_._2).avg)
}

