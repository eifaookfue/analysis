package jp.co.nri.nefs.tool.util.data.format
import java.time.ZoneId
import java.util.Date

import jp.co.nri.nefs.tool.util.data.LineError
import org.apache.poi.ss.usermodel.{Cell, CellValue, DateUtil, Row}

import scala.language.implicitConversions
import scala.util.control.Exception._

object Formats {

  final val CELL_HAS_SOME_ERRORS = "The Cell has already some errors."
  final val UNEXPECTED_CELL_TYPE = "Unexpected cell type."
  final val NO_VALUES = "There are no values."

  def ignoredFormat[A](value: A): Formatter[A] = new Formatter[A] {
    override def bind(index: Int, row: Row): Either[Seq[LineError], A] = Right(value)

    override def unbind(index: Int, value: A, row: Row): Unit = {}

  }

  implicit def bigDecimalToJavaBigDecimal(b: BigDecimal): java.math.BigDecimal = b.underlying

  implicit def stringFormat: Formatter[String] = stringFormat(false)

  def trimStringFormat: Formatter[String] = stringFormat(true)

  private def stringFormat(isTrim: Boolean): Formatter[String] = new Formatter[String] {
    override def bind(index: Int, row: Row): Either[Seq[LineError], String] = {
      val cell = if (row == null) null else row.getCell(index)
      if (cell == null) Right(null) else {
        cell.getCellType match {
          case Cell.CELL_TYPE_BLANK => Right(null)
          case Cell.CELL_TYPE_BOOLEAN => Right(cell.getBooleanCellValue.toString)
          case Cell.CELL_TYPE_ERROR => Left(Seq(LineError(index, CELL_HAS_SOME_ERRORS)))
          case Cell.CELL_TYPE_FORMULA =>
            getFormulaStringValue(cell, isTrim).left.map(s => Seq(LineError(index, s)))
          case Cell.CELL_TYPE_NUMERIC => Right(BigDecimal(cell.getNumericCellValue).underlying.stripTrailingZeros().toPlainString)
          case Cell.CELL_TYPE_STRING =>
            val str = if (isTrim) cell.getStringCellValue.trim else cell.getStringCellValue
            Right(str)
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

  def sqlTimestampFormat(pattern: String, timeZone: java.util.TimeZone = java.util.TimeZone.getDefault): Formatter[java.sql.Timestamp] =
    new Formatter[java.sql.Timestamp] {
      import java.time.LocalDateTime

      private val formatter = java.time.format.DateTimeFormatter.ofPattern(pattern).withZone(timeZone.toZoneId)
      private def timestampParse(data: String) = java.sql.Timestamp.valueOf(LocalDateTime.parse(data, formatter))

      override def bind(index: Int, row: Row): Either[Seq[LineError], java.sql.Timestamp] =
        parsing(timestampParse, "error.timestamp", Nil)(index, row)

      override def unbind(index: Int, value: java.sql.Timestamp, row: Row): Unit =
        row.getCell(index).setCellValue(value.toLocalDateTime.format(formatter))
    }

  implicit val sqlTimestampFormat: Formatter[java.sql.Timestamp] = sqlTimestampFormat("yyyy-MM-dd HH:mm:ss")

  private def optionalDate(index: Int, row: Row): Option[Date] = {
    Option(row).map(_.getCell(index)).flatMap { cell =>
      if (cell.getCellType == Cell.CELL_TYPE_NUMERIC && DateUtil.isCellDateFormatted(cell))
        Some(cell.getDateCellValue)
      else
        None
    }
  }

  def localDateFormat(pattern: String): Formatter[java.time.LocalDate] = new Formatter[java.time.LocalDate] {

    import java.time.LocalDate

    private val formatter: java.time.format.DateTimeFormatter = java.time.format.DateTimeFormatter.ofPattern(pattern)
    private def localDateParse(data: String): LocalDate = LocalDate.parse(data, formatter)

    override def bind(index: Int, row: Row): Either[Seq[LineError], LocalDate] = {
      optionalDate(index, row).map { date =>
        date.toInstant.atZone(ZoneId.systemDefault()).toLocalDate
      } match {
        case Some(d) => Right(d)
        case None =>
          parsing(localDateParse, "error.date", Nil)(index, row)
      }
    }

    override def unbind(index: Int, value: LocalDate, row: Row): Unit =
      row.getCell(index).setCellValue(value.format(formatter))
  }

  implicit val localDateFormat: Formatter[java.time.LocalDate] = localDateFormat("HH:mm:ss")

  private def getFormulaCellValue(cell: Cell): CellValue = {
    val book = cell.getSheet.getWorkbook
    val helper = book.getCreationHelper
    val evaluator = helper.createFormulaEvaluator()
    evaluator.evaluate(cell)
  }

  // CellとCellValueで同じメソッド名を提供しているがインターフェースが別のため共通化できない
  def getFormulaStringValue(cell: Cell, isTrim: Boolean): Either[String, String] = {
    if (cell == null) Right(null) else {
      val cellValue = getFormulaCellValue(cell)
      cellValue.getCellType match {
        case Cell.CELL_TYPE_BLANK => Right(null)
        case Cell.CELL_TYPE_BOOLEAN => Right(cellValue.getBooleanValue.toString)
        case Cell.CELL_TYPE_ERROR => Left(CELL_HAS_SOME_ERRORS)
        case Cell.CELL_TYPE_FORMULA => Left(UNEXPECTED_CELL_TYPE)
        case Cell.CELL_TYPE_NUMERIC => Right(cellValue.getNumberValue.toString)
        case Cell.CELL_TYPE_STRING =>
          val str = if (isTrim) cellValue.getStringValue.trim else cellValue.getStringValue
          Right(str)
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
