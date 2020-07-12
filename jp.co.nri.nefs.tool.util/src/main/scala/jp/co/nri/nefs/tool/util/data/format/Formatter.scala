package jp.co.nri.nefs.tool.util.data.format

import jp.co.nri.nefs.tool.util.data.LineError
import org.apache.poi.ss.usermodel.Row

trait Formatter[T] {
  val format: Option[(String, Seq[Any])] = None
  def bind(index: Int, row: Row): Either[Seq[LineError], T]
  def unbind(index: Int, value: T, row: Row): Unit
}
