package jp.co.nri.nefs.tool.apllog

import java.nio.file.{Files, Path, Paths, StandardCopyOption}
import java.text.SimpleDateFormat
import java.util.Date
import java.util.stream.Collectors
import jp.co.nri.nefs.tool.utils.FileUtils

import scala.collection.JavaConverters._

object Bringin{
  def main(args: Array[String]): Unit = {


    val usage = """
        Usage: Bringin --cachedir cachedir --afterdate afterdate(yyyy/MM/dd HH:mm:ss) --outputdir outputdir
        """

    if (args.length == 0){
      println(usage)
      sys.exit(1)
    }

    val argList = args.toList
    type OptionMap = Map[Symbol, String]

    def nextOption(map: OptionMap, list: List[String]): OptionMap = {
      //def isSwitch(s: String) = (s(0) == '-')

      list match {
        case Nil => map
        case "--cachedir" :: value :: tail =>
          nextOption(map ++ Map('cachedir -> value), tail)
        case "--afterdate" :: value :: tail =>
          nextOption(map ++ Map('afterdate -> value), tail)
        case "--outputdir" :: value :: tail =>
          nextOption(map ++ Map('outputdir -> value), tail)
        case _ => println("Unknown option")
          sys.exit(1)
      }
    }

    val options = nextOption(Map(), argList)

    def checkAndGet(key: Symbol): String = {
      options.get(key) match {
        case Some(s) => s
        case _ => println(s"${key.toString().drop(1)} is missing.")
          sys.exit(1)
      }
    }

    val cachedir = checkAndGet('cachedir)
    val afterdate = checkAndGet('afterdate)
    val outputdir = checkAndGet('outputdir)

    val len = cachedir.split("\\\\").length
    val sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
    val base = sdf.parse(afterdate)
    val start = Paths.get(cachedir)

    //Streamのままfor式にいれると、エラーになってしまう
    val fileList = Files.walk(start).collect(Collectors.toList())
    val bringSet:Set[Path] = (for (p <- fileList.asScala
      if p.toFile.isFile
      if p.toFile.lastModified() > base.getTime)
      yield p.getParent)(collection.breakOut) //コレクションの型を変更するにはbreakoutを使う。戻り値の型に変更にしてくれる
    bringSet.foreach(println _)

    val now = "%tY%<tm%<td%<tH%<tM%<tS" format new Date
    val newdir = now + "_" + afterdate.substring(0,4) + afterdate.substring(5,7) + afterdate.substring(8,10)
    val outpath = Paths.get(outputdir, newdir)
    Files.createDirectories(outpath)

    bringSet.foreach(p => FileUtils.copyDir(start, p, outpath))
//    Files.copy

    /*Files.walk(start).filter(_.toFile.isFile)
      .filter(_.toFile.lastModified() > base.getTime)
      .map(p => {
        val sub = p.toString.substring(len+1, p.toString.indexOf("\\",len+1))
        start.resolve(sub)
      })
      .collect(Collectors.toSet)*/

      /*.forEach(p => {
        val sub = p.toString.substring(len+1, p.toString.indexOf("\\",len+1))
        val n = start.resolve(sub)
        println(s"$n $p update=${sdf.format(p.toFile.lastModified())}")
      })*/

  }
}

