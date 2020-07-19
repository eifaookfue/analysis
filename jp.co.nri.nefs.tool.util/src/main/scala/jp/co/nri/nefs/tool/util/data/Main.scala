package jp.co.nri.nefs.tool.util.data

import java.nio.file.{Files, Paths}

import org.apache.poi.ss.usermodel.WorkbookFactory
import jp.co.nri.nefs.tool.util.data.Lines._
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import scala.reflect.runtime.universe._

/*
Lines
def write[T](line: Line[T], value: T, path: Path, sheetName: String, start: Int, header: Boolean = truem headers: Option[Seq[String]] = None)(implicit m: ClassTag[T])
・LineのclassTag Tをとれるか

 */

object Main {
  def main(args: Array[String]): Unit = {
    println(getMethods[IOI])
    val in = Files.newInputStream(Paths.get("C:/Users/user/Documents/Book1.xlsx"))
    val wb = WorkbookFactory.create(in)
    val sheet = wb.getSheetAt(0)
    //val row = sheet.getRow(1)
    val ioiLine = Line(mapping(
      "0" -> text,
      "1" -> text,
      "2" -> text,
      "3" -> number,
      "2x3@4" -> list(mapping(
        "0" -> text,
        "1" -> number
      )(Order.apply)(Order.unapply))
    )(IOI.apply)(IOI.unapply)
    )
    //val ioi = ioiLine.bind(row).get

    val iois = for {
      i <- 1 to 6
      row = sheet.getRow(i)
      ioi = ioiLine.bind(row).get
    } yield ioi

    iois.foreach(println)





    //ioiLine.bind(row).fold(line => println(line.errors), value => println(value))
    //println(ioi)
    in.close()
    val out = Files.newOutputStream(Paths.get("C:/Users/user/Documents/Book2.xlsx"))
    val wb2 = new XSSFWorkbook()
    val sheet2 = wb2.createSheet()
    //val row2 = sheet2.createRow(0)
    //row2.createCell(0).setCellValue("abc")
    //ioiLine.mapping.unbind(ioi, sheet2.createRow(1))

    for {
      (ioi, i) <- iois.zipWithIndex
      row = sheet2.createRow(i)
    } ioiLine.mapping.unbind(ioi, row)


    wb2.write(out)
    out.close()

  }

  def getMethods[T: TypeTag]: List[String] =
    typeOf[T].members.sorted.collect {
      case m: MethodSymbol if m.isCaseAccessor => m.name.toString
    }
}

case class WindowDetail(str1: String, str2: String)
case class IOI(ioiId: String, bsType: String, symbol: String, quantity: Int, order: List[Order])
case class Order(orderId: String, orderQty: Int)
