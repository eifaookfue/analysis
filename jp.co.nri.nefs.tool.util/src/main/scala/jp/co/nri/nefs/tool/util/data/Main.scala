package jp.co.nri.nefs.tool.util.data

import java.nio.file.{Files, Paths}
import org.apache.poi.ss.usermodel.WorkbookFactory
import jp.co.nri.nefs.tool.util.data.Lines._

object Main {
  def main(args: Array[String]): Unit = {
    val in = Files.newInputStream(Paths.get("C:/Users/user/Documents/Book1.xlsx"))
    val wb = WorkbookFactory.create(in)
    val sheet = wb.getSheetAt(0)
    val row = sheet.getRow(1)
    val windowDetailLine = Line(mapping(
      1 -> text,
      2 -> text,
      3 -> text,
      4 -> number,
      5 -> text,
      6 -> number
    )(IOI.apply)(IOI.unapply)
    )
    val windowDetail = windowDetailLine.bind(row).get
    println(windowDetail)
    in.close()
  }
}

case class WindowDetail(str1: String, str2: String)
case class IOI(ioiId: String, bsType: String, symbol: String, quantity: Int, orderId: String, orderQty: Int)
