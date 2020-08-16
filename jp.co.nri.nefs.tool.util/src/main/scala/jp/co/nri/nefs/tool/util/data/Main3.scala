package jp.co.nri.nefs.tool.util.data

import scala.reflect.runtime.{universe => ru}
import ru._

object Main3 {

  def main(args: Array[String]): Unit = {
    println(paramSize(getType[P1]))
  }

  /*
    Return the parameter size.
    If a parameter has type parameter, get the size of the parameter recursively.
   */
  def paramSize(t: Type): Int = {
    val constructor = t.decl(termNames.CONSTRUCTOR).asMethod
    val paramList = constructor.paramLists.head
    paramList.map(_.typeSignature.typeArgs.headOption.map(paramSize).getOrElse(1)).sum
  }

  def getType[T: TypeTag]: Type = typeOf[T]
}

// P1のパラメタ数として3ではなく4を取得したい！
case class P1(p1: String, p2: Int, p3: List[C1])
case class C1(c1: String, c2: Int, c3: List[G1])
case class G1(g1: String, g2: Int)
