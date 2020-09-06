package jp.co.nri.nefs.tool.util.data.format
import jp.co.nri.nefs.tool.util.data.LineError
import org.apache.poi.ss.usermodel.{Cell, CellValue, Row}
import scala.language.implicitConversions
import scala.util.control.Exception._

object Formats {

  final val CELL_HAS_SOME_ERRORS = "The Cell has already some errors."
  final val UNEXPECTED_CELL_TYPE = "Unexpected cell type."
  final val NO_VALUES = "There are no values."

  implicit def bigDecimalToJavaBigDecimal(b: BigDecimal): java.math.BigDecimal = b.underlying
  implicit def stringFormat: Formatter[String] = new Formatter[String] {
    override def bind(index: Int, row: Row): Either[Seq[LineError], String] = {
      val cell = row.getCell(index)
      if (cell == null) Right(null) else {
        cell.getCellType match {
          case Cell.CELL_TYPE_BLANK => Right(null)
          case Cell.CELL_TYPE_BOOLEAN => Right(cell.getBooleanCellValue.toString)
          case Cell.CELL_TYPE_ERROR => Left(Seq(LineError(index, CELL_HAS_SOME_ERRORS)))
          case Cell.CELL_TYPE_FORMULA =>
            getFormulaStringValue(cell).left.map(s => Seq(LineError(index, s)))
          case Cell.CELL_TYPE_NUMERIC => Right(cell.getNumericCellValue.toString)
          case Cell.CELL_TYPE_STRING => Right(cell.getStringCellValue)
        }
      }
    }

    override def unbind(index: Int, value: String, row: Row): Unit = {
      row.createCell(index).setCellValue(value)
    }
  }

  implicit def numberFormat: Formatter[Int] = new Formatter[Int] {
    override def bind(index: Int, row: Row): Either[Seq[LineError], Int] = {
      val cell = row.getCell(index)
      if (cell == null) Left(Seq(LineError(index, NO_VALUES))) else {
        cell.getCellType match {
          case Cell.CELL_TYPE_BLANK => Left(Seq(LineError(index, NO_VALUES)))
          case Cell.CELL_TYPE_BOOLEAN => Right(if (cell.getBooleanCellValue) 1 else 0)
          case Cell.CELL_TYPE_ERROR => Left(Seq(LineError(index, CELL_HAS_SOME_ERRORS)))
          case Cell.CELL_TYPE_FORMULA =>
            getFormulaIntValue(cell).left.map(s => Seq(LineError(index, s)))
          case Cell.CELL_TYPE_STRING =>
            (catching(classOf[NumberFormatException]) either cell.getStringCellValue.toInt).left.map { e =>
              Seq(LineError(index, e.toString))
            }
          case Cell.CELL_TYPE_NUMERIC => Right(cell.getNumericCellValue.toInt)
        }
      }
    }

    override def unbind(index: Int, value: Int, row: Row): Unit = {
      row.createCell(index).setCellValue(value)
    }
  }

  private def getFormulaCellValue(cell: Cell): CellValue = {
    val book = cell.getSheet.getWorkbook
    val helper = book.getCreationHelper
    val evaluator = helper.createFormulaEvaluator()
    evaluator.evaluate(cell)
  }

  // CellとCellValueで同じメソッド名を提供しているがインターフェースが別のため共通化できない
  def getFormulaStringValue(cell: Cell): Either[String, String] = {
    if (cell == null) Right(null) else {
      val cellValue = getFormulaCellValue(cell)
      cellValue.getCellType match {
        case Cell.CELL_TYPE_BLANK => Right(null)
        case Cell.CELL_TYPE_BOOLEAN => Right(cellValue.getBooleanValue.toString)
        case Cell.CELL_TYPE_ERROR => Left(CELL_HAS_SOME_ERRORS)
        case Cell.CELL_TYPE_FORMULA => Left(UNEXPECTED_CELL_TYPE)
        case Cell.CELL_TYPE_NUMERIC => Right(cellValue.getNumberValue.toString)
        case Cell.CELL_TYPE_STRING => Right(cellValue.getStringValue)
      }
    }
  }

  def getFormulaIntValue(cell: Cell): Either[String, Int] = {
    if (cell == null) Left(NO_VALUES) else {
      val cellValue = getFormulaCellValue(cell)
      cellValue.getCellType match {
        case Cell.CELL_TYPE_BLANK => Left(NO_VALUES)
        case Cell.CELL_TYPE_BOOLEAN => Right(if (cellValue.getBooleanValue) 1 else 0)
        case Cell.CELL_TYPE_ERROR => Left(CELL_HAS_SOME_ERRORS)
        case Cell.CELL_TYPE_FORMULA => Left(UNEXPECTED_CELL_TYPE)
        case Cell.CELL_TYPE_STRING =>
          (catching(classOf[NumberFormatException]) either cellValue.getNumberValue.toInt).left.map(_.toString)
      }
    }
  }


}
