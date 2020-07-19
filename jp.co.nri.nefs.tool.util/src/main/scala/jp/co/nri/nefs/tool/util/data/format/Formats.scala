package jp.co.nri.nefs.tool.util.data.format
import jp.co.nri.nefs.tool.util.data.LineError
import org.apache.poi.ss.usermodel.{Cell, Row}
import scala.language.implicitConversions

object Formats {
  implicit def bigDecimalToJavaBigDecimal(b: BigDecimal): java.math.BigDecimal = b.underlying
  implicit def stringFormat: Formatter[String] = new Formatter[String] {
    override def bind(index: Int, row: Row): Either[Seq[LineError], String] = {
      Option(row.getCell(index)).flatMap{ cell =>
        cell.getCellType match {
         case Cell.CELL_TYPE_BLANK => None
          case Cell.CELL_TYPE_BOOLEAN => Some(cell.getBooleanCellValue.toString)
          case Cell.CELL_TYPE_STRING => Some(cell.getStringCellValue)
          case Cell.CELL_TYPE_ERROR => None
          //TODO
          //case Cell.CELL_TYPE_FORMULA => getFormulaValue(clazz)
          // case Cell.CELL_TYPE_NUMERIC => Some(BigDecimal(cell.getNumericCellValue).stripTrailingZeros().intValue())
          case Cell.CELL_TYPE_NUMERIC => Some(BigDecimal(cell.getNumericCellValue).stripTrailingZeros().toString)
        }
      }.toRight(Seq(LineError(index, "error.required", Nil)))

    }

    override def unbind(index: Int, value: String, row: Row): Unit = {
      row.createCell(index).setCellValue(value)
    }
  }

  implicit def numberFormat: Formatter[Int] = new Formatter[Int] {
    override def bind(index: Int, row: Row): Either[Seq[LineError], Int] = {
      //Some(row.getCell(index).getNumericCellValue.toInt).toRight(Seq(LineError(index, "error.required.", Nil)))
      Option(row.getCell(index)).flatMap{ cell =>
        cell.getCellType match {
          case Cell.CELL_TYPE_BLANK => None
          case Cell.CELL_TYPE_BOOLEAN => None
          case Cell.CELL_TYPE_STRING => Some(cell.getStringCellValue.toInt)
          case Cell.CELL_TYPE_ERROR => None
          //TODO
          //case Cell.CELL_TYPE_FORMULA => getFormulaValue(clazz)
          //Some(BigDecimal(doubleValue).stripTrailingZeros().toString.asInstanceOf[T])
          case Cell.CELL_TYPE_NUMERIC => Some(cell.getNumericCellValue.toInt)
        }
      }.toRight(Seq(LineError(index, "error.required", Nil)))
    }

    override def unbind(index: Int, value: Int, row: Row): Unit = {
      row.createCell(index).setCellValue(value)
    }
  }
}
