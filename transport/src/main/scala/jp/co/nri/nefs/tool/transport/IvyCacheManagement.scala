package jp.co.nri.nefs.tool.transport

import java.nio.file._
import java.util.stream.Collectors

import scala.collection.JavaConverters._


object IvyCacheManagement {
  type OptionMap = Map[Symbol, String]
  val usage = """
        Usage: jp.co.nri.nefs.tool.transport.IvyCacheManagement --inputdir dir
        """

  def main(args: Array[String]): Unit = {
    val options = nextOption(Map(), args.toList)
    val path = getOption(options)
    val files = Files.walk(path).collect(Collectors.toList()).asScala
    val sequence = for (file <- files if file.getFileName.toString.endsWith("properties");
      (fileName, key, location) <- getList(file) if location contains "http" )
      yield (fileName, key, location)
    val outPath = Paths.get("D:\\tmp\\remote.txt")
    val list = (for ((fileName, _, _) <- sequence) yield fileName).asJava
    Files.write(outPath, list)
    sequence.foreach(println _)
    /*for ((fileName, key, location) <- sequence; if !(location.contains(".m2")))
      println(fileName, key, location)*/
  }

  def getList(path: Path): List[(String, String, String)] = {
    val regex = """(.*)(\.location)=(.*)""".r
    val list = Files.readAllLines(path).asScala.toList
    list.collect {
      case regex(key, _, location) => (path.toString, key, location)
    }
  }


  def nextOption(map: OptionMap, list: List[String]): OptionMap = {
    list match {
      case Nil => map
      case "--inputdir" :: value :: tail =>
        nextOption(map ++ Map(Symbol("inputdir") -> value), tail)
      case _ => println("Unknown option")
        println(usage)
        throw new java.lang.IllegalArgumentException
    }
  }

  def getOption(options: OptionMap): Path = {
    val inputdir = options.get(Symbol("inputdir"))
    inputdir match {
      case Some(dir) =>
        Paths.get(dir)
      case None =>
        println(usage)
        throw new java.lang.IllegalArgumentException()
    }
  }





}