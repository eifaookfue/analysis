package jp.co.nri.nefs.tool.util.data

import org.apache.poi.ss.usermodel.Row
import jp.co.nri.nefs.tool.util.data.Lines._
import scala.reflect.runtime.{universe => ru}
import ru._

object Main2 {
  def main(args: Array[String]): Unit = {
    val ioi = IOI("1", "B", "6758", 1000, List(Order("1", 1000), Order("2", 2000)))
    println(getTypeTag(ioi).tpe)
    val cSymbol = getTypeTag(ioi).tpe.decl(ru.termNames.CONSTRUCTOR)
    val cMethod = cSymbol.asMethod
    println(cMethod)
    val paramList = cMethod.paramLists.head
    paramList.foreach { param =>
      println(param.name.toString)
      param.typeSignature match {
        case TypeRef(_, symbol, _) if symbol.toString.contains("List") =>
          println("List")
        case _ =>
      }

    }

    val ioiLine = Line(mapping(
      key(0) -> text,
      key(1) -> text,
      key(2) -> text,
      key(3) -> number,
      key(4) -> list(mapping(
        key(1) -> text,
        key(2) -> number
      )(Order.apply)(Order.unapply))
    )(IOI.apply)(IOI.unapply)
    )
    println(s"ioiLineMapping typeArgs = ${getTypeTag(ioiLine.mapping).tpe.typeArgs}")
    val ttt = getTypeTag(ioiLine.mapping).tpe.typeArgs.head

    val cMethod2 = ttt.decl(ru.termNames.CONSTRUCTOR).asMethod
    val paramList2 = cMethod2.paramLists.head
    paramList2.foreach { param =>
      println(param.name.toString)
      param.typeSignature match {
        case TypeRef(_, symbol, _) => println(symbol)
      }
    }
    val windowDetail = WindowDetail("abc", "def", List("A", "B"))


    /*paramList.map(_.typeSignature).foreach { case t @ TypeRef(_, symbol, _) =>
        println(t)
        if (symbol.toString.contains("List"))
          println("List")
    }*/


    /*paramList.map(_.typeSignature).foreach{ t =>
      t match {
        case TypeRef(a, b, c) =>
          println(t)
          println(a, b, c)
          b.asType

          if (b.isClass){
            println(b.asClass == definitions.ListClass.toType.typeSymbol)
          }

          println(b.typeSignature <:< typeOf[List[Any]])

      }

    }*/
  }

  def getTypeTag[T: ru.TypeTag](obj: T) = ru.typeTag[T]
}


case class WindowDetail(str1: String, str2: String, list3: List[String]) {
  val constructor = Main2.getTypeTag(this).tpe.decl(ru.termNames.CONSTRUCTOR).asMethod
  val paramList: List[String] = constructor.paramLists.head.map(_.name.toString)
  val field1ParamNames: Option[Seq[String]] = None
  val field2ParamNames: Option[Seq[String]] = None
//  val field3ParamNames: Option[Seq[String]] = Some(Seq("s1", "s2"))
  val field3ParamNames: Option[Seq[String]] = None
  val l = for {
    (childNames, parentName) <- Seq(field1ParamNames, field2ParamNames, field3ParamNames) zip paramList
  } yield {
    childNames.map{cNames =>
      for {
        i <- 0 to 1
        cName <- cNames
      } yield s"$parentName[$i].$cName"
    }.getOrElse(Seq(parentName))
  }
  println(Option(l.flatten).filter(_.nonEmpty))


  /*paramList.foreach { param =>
    param.typeSignature match {
      case TypeRef(_, symbol, _) => println(s"${symbol}(${param.name.toString})")
    }
  }*/
}