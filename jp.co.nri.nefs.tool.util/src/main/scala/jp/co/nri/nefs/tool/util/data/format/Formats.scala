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
      val cell = if (row == null) null else row.getCell(index)
      if (cell == null) Right(null) else {
        cell.getCellType match {
          case Cell.CELL_TYPE_BLANK => Right(null)
          case Cell.CELL_TYPE_BOOLEAN => Right(cell.getBooleanCellValue.toString)
          case Cell.CELL_TYPE_ERROR => Left(Seq(LineError(index, CELL_HAS_SOME_ERRORS)))
          case Cell.CELL_TYPE_FORMULA =>
            getFormulaStringValue(cell).left.map(s => Seq(LineError(index, s)))
          case Cell.CELL_TYPE_NUMERIC => Right(BigDecimal(cell.getNumericCellValue).underlying.stripTrailingZeros().toPlainString)
          case Cell.CELL_TYPE_STRING => Right(cell.getStringCellValue)
        }
      }
    }

    override def unbind(index: Int, value: String, row: Row): Unit = {
      row.createCell(index).setCellValue(value)
    }
  }

  /**
    * Helper for formatters binders
    * @param parse Function parsing a String value into a T value, throwing an exception in case of failure
    * @param errArgs Error to set in case of parsing failure
    * @param index Index of the field to parse
    * @param row Excel row data
    */
  def parsing[T](parse: String => T, errMsg: String, errArgs: Seq[Any])(index: Int, row: Row): Either[Seq[LineError], T] = {
    stringFormat.bind(index, row).right.flatMap { s =>
      scala.util.control.Exception.allCatch[T]
        .either(parse(s))
        .left.map(_ => Seq(LineError(index, errMsg, errArgs)))
    }
  }

  implicit def numberFormatter[T](convert: String => T, setter: (Cell, T) => Unit, real: Boolean = false): Formatter[T] = {
    val errorString = if (real) "error.real" else "error.number"
    new Formatter[T] {
      override def bind(index: Int, row: Row): Either[Seq[LineError], T] =
        parsing(convert, errorString, Nil)(index, row)

      override def unbind(index: Int, value: T, row: Row): Unit = {
//        row.getCell(index).setCellValue(value)
        setter(row.getCell(index), value)

      }
    }
  }

  implicit def longFormat: Formatter[Long] = numberFormatter(_.toLong, (cell, value) => cell.setCellValue(value))

  implicit def intFormat: Formatter[Int] = numberFormatter(_.toInt, (cell, value) => cell.setCellValue(value))

  implicit def shortFormat: Formatter[Short] = numberFormatter(_.toShort, (cell, value) => cell.setCellValue(value))

  implicit def floatFormat: Formatter[Float] = numberFormatter(_.toFloat, (cell, value) => cell.setCellValue(value), real = true)

  implicit def doubleFormat: Formatter[Double] = numberFormatter(_.toDouble, (cell, value) => cell.setCellValue(value), real = true)


  /*implicit def numberFormat: Formatter[Int] = new Formatter[Int] {
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
  }*/

  implicit val bigDecimalFormat: Formatter[BigDecimal] = bigDecimalFormat(None)

  def bigDecimalFormat(precision: Option[(Int, Int)]): Formatter[BigDecimal] = new Formatter[BigDecimal] {
    def bind(index: Int, row: Row): Either[Seq[LineError], BigDecimal] = {
      Formats.stringFormat.bind(index, row).right.flatMap { s =>
        scala.util.control.Exception.allCatch[BigDecimal]
          .either {
            val bd = BigDecimal(s)
            precision.map({
              case (p, scale) =>
                if (bd.precision - bd.scale > p - scale) {
                  throw new java.lang.ArithmeticException("Invalid precision")
                }
                bd.setScale(scale)
            }).getOrElse(bd)
          }
          .left.map { _ =>
          Seq(
            precision match {
              case Some((p, scale)) => LineError(index, "error.real.precision", Seq(p, scale))
              case None => LineError(index, "error.real", Nil)
            }
          )
        }
      }
    }
    def unbind(index: Int, value: BigDecimal, row: Row): Unit = {
      val bd = precision.map(p => value.setScale(p._2)).getOrElse(value)
      row.getCell(index).setCellValue(bd.toDouble)
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
