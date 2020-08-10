package jp.co.nri.nefs.tool.util.data

import java.nio.file.{Files, Paths}

import org.apache.poi.ss.usermodel.WorkbookFactory
import jp.co.nri.nefs.tool.util.data.Lines._
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import scala.reflect.runtime.universe._

/*
Lines
def write[T](line: Line[T], value: T, path: Path, sheetName: String, start: Int, header: Boolean = true headers: Option[Seq[String]] = None)(implicit m: ClassTag[T])
・LineのclassTag Tをとれるか

 */

object Main {
  def main(args: Array[String]): Unit = {
    //println(getMethods[IOI])
    val in = Files.newInputStream(Paths.get("C:/Users/user/Documents/Book1.xlsx"))
    val wb = WorkbookFactory.create(in)
    val sheet = wb.getSheetAt(0)
    //val row = sheet.getRow(1)
    val ioiLine = Line(mapping(
      key(0) -> text,
      key(1) -> text,
      key(2) -> text,
      key(3) -> number,
      key(4, 3) -> list(mapping(
        key(0) -> text,
        key(1) -> number
      )(Order.apply)(Order.unapply))
    )(IOI.apply)(IOI.unapply)
    )
    //val ioi = ioiLine.bind(row).get

    ioiLine.mapping.paramNames.foreach(println)

    val iois = for {
      i <- 1 to 6
      row = sheet.getRow(i)
      ioi = ioiLine.bind(row).get
    } yield ioi

    iois.foreach(println)




    in.close()

/*
    val out = Files.newOutputStream(Paths.get("C:/Users/user/Documents/Book2.xlsx"))
    val wb2 = new XSSFWorkbook()
    val sheet2 = wb2.createSheet()

    for {
      (ioi, i) <- iois.zipWithIndex
      row = sheet2.createRow(i)
    } ioiLine.mapping.unbind(ioi, row)


    wb2.write(out)
    out.close()
*/

    val path = Paths.get("C:/Users/user/Documents/Book2.xlsx")
    Lines.write(ioiLine, iois, path)

  }

  def getMethods[T: TypeTag]: List[String] =
    typeOf[T].members.sorted.collect {
      case m: MethodSymbol if m.isCaseAccessor => m.name.toString
    }
}


case class IOI(ioiId: String, bsType: String, symbol: String, quantity: Int, order: List[Order])
case class Order(orderId: String, orderQty: Int)
