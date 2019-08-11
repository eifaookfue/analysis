package jp.co.nri.nefs.tool.transport

import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes
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
    Files.walkFileTree(path, new Visitor)
    val files = Files.walk(path).collect(Collectors.toList()).asScala.toSeq
    val sequence = for (file <- files if file.getFileName.toString.endsWith("properties");
      (fileName, key, location) <- getList(file) if !(location contains "http") )
      yield (fileName, key, location)
    sequence.foreach(println _)
  }

  class Visitor extends SimpleFileVisitor[Path] {
    override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
      if (file.getFileName.toString.endsWith("properties")) {
        for ((fileName, key, location) <- getList(file) if !(location contains "http")) {
          println(s"fileName=$fileName, key=$key, location=$location")
        }
      }
      FileVisitResult.CONTINUE
    }
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
    if (inputdir.isEmpty){
      println(usage)
      throw new java.lang.IllegalArgumentException
    }
    inputdir match {
      case Some(dir) =>
        Paths.get(dir)
      case None =>
        println(usage)
        throw new java.lang.IllegalArgumentException()
    }
  }





}