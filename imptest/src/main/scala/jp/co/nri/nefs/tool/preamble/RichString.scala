package jp.co.nri.nefs.tool.preamble
import scala.language.implicitConversions

object RichString {
  val symbolMap = Map("0001" -> "6501", "0002" -> "6502")
  implicit def string2toSymbol(x: String): RichString = new RichString(x)
}

class RichString(s: String) {
  def toSymbol: String = {
    RichString.symbolMap(s)
  }
}