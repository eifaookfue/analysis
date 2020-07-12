package jp.co.nri.nefs.tool.util.data.format
import jp.co.nri.nefs.tool.util.data.LineError
import org.apache.poi.ss.usermodel.{Cell, Row}

object Formats {
  implicit def stringFormat: Formatter[String] = new Formatter[String] {
    override def bind(index: Int, row: Row): Either[Seq[LineError], String] = {
      //Some(row.getCell(index).getStringCellValue).toRight(Seq(LineError(index, "error.required", Nil)))

      val cell = row.getCell(index)
      (cell.getCellType match {
        case Cell.CELL_TYPE_BLANK => None
        case Cell.CELL_TYPE_BOOLEAN => Some(cell.getBooleanCellValue.toString)
        case Cell.CELL_TYPE_STRING => Some(cell.getStringCellValue)
        case Cell.CELL_TYPE_ERROR => None
        //TODO
        //case Cell.CELL_TYPE_FORMULA => getFormulaValue(clazz)
        case Cell.CELL_TYPE_NUMERIC => Some(cell.getNumericCellValue.toString)
      }).toRight(Seq(LineError(index, "error.required", Nil)))

    }

    override def unbind(index: Int, value: String, row: Row): Unit = {
      row.getCell(index).setCellValue(value)
    }
  }

  implicit def numberFormat: Formatter[Int] = new Formatter[Int] {
    override def bind(index: Int, row: Row): Either[Seq[LineError], Int] = {
      Some(row.getCell(index).getNumericCellValue.toInt).toRight(Seq(LineError(index, "error.required.", Nil)))
    }

    override def unbind(index: Int, value: Int, row: Row): Unit = {
      row.getCell(index).setCellValue(value)
    }
  }
}
