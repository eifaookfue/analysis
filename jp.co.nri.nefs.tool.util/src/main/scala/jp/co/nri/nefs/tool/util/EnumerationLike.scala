package jp.co.nri.nefs.tool.util

trait EnumerationLike {
  self: Enumeration =>

  def withNameOp(s: String): Option[Value] = {
    try {
      Some(withName(s))
    } catch {
      case _: Exception => None
    }

  }
}
