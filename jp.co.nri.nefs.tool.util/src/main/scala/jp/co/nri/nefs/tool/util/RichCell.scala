package jp.co.nri.nefs.tool.util

import java.sql.Timestamp
import java.text.SimpleDateFormat

import org.apache.poi.ss.format.CellFormat
import org.apache.poi.ss.usermodel.{BuiltinFormats, Cell, DateUtil}

import scala.language.implicitConversions
import scala.reflect.ClassTag

object RichCell {
  implicit def cellToRichCell(cell: Cell): RichCell = new RichCell(cell)
}

class RichCell(cell: Cell) {
  lazy val format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS")
  lazy val ERROR_MESSAGE_DATE = "format only supports String or java.util.Date or java.sql.Timestamp"
  lazy val ERROR_MESSAGE_STR_NUMERIC = "format only supports Int or Long or BigDecimal or String"
  // ScalaのBigDecimalにはstripTrailingZerosメソッドが存在しないため暗黙変換を用いる
  // https://stackoverflow.com/questions/9154416/whats-the-right-way-to-drop-trailing-zeros-of-a-bigdecimal-in-scala
  // java.math.BigDecimalからBigDecimalへは自動で暗黙変換される
  implicit def bigDecimalToJavaBigDecimal(b: BigDecimal): java.math.BigDecimal = b.underlying

  def getValue[T](implicit m: ClassTag[T]): Option[T] = {
    val clazz = m.runtimeClass
    // https://qiita.com/panage/items/4476ff91d1fcec67f525
    // ユーザー定義型で日付を設定している場合
    if (BuiltinFormats.FIRST_USER_DEFINED_FORMAT_INDEX <= cell.getCellStyle.getDataFormat) {
      val cellFormat = CellFormat.getInstance(cell.getCellStyle.getDataFormatString)
      val cellFormatResult = cellFormat.apply(cell)
      val text = cellFormatResult.text
      if (clazz.equals(classOf[java.util.Date])){
        Some(format.parse(text).asInstanceOf[T])
      } else if (clazz.equals(classOf[java.sql.Timestamp])) {
        Some(new java.sql.Timestamp(format.parse(text).getTime).asInstanceOf[T])
      } else if (clazz.equals(classOf[String])) {
        Some(text.asInstanceOf[T])
      } else {
        throw new java.lang.RuntimeException("User defined " + ERROR_MESSAGE_DATE)
      }
    } else if (cell.getCellType == Cell.CELL_TYPE_NUMERIC && DateUtil.isCellDateFormatted(cell)){ // Excel標準の日付書式を使っている場合
      val date = cell.getDateCellValue
      if (clazz.equals(classOf[java.util.Date])){
        Some(date.asInstanceOf[T])
      } else if (clazz.equals(classOf[java.sql.Timestamp])) {
        Some(new Timestamp(date.getTime).asInstanceOf[T])
      } else if (clazz.equals(classOf[String])) {
        Some(format.format(date).asInstanceOf[T])
      } else {
        throw new java.lang.RuntimeException("Excel normal " + ERROR_MESSAGE_DATE)
      }
    } else {
      cell.getCellType match {
        case Cell.CELL_TYPE_BLANK => None
        case Cell.CELL_TYPE_BOOLEAN => Some(cell.getBooleanCellValue.asInstanceOf[T])
        case Cell.CELL_TYPE_STRING =>
          getStringValue(clazz, cell.getStringCellValue)
        case Cell.CELL_TYPE_ERROR => None
        case Cell.CELL_TYPE_FORMULA => getFormulaValue(clazz)
        case Cell.CELL_TYPE_NUMERIC =>
          getNumericValue(clazz, cell.getNumericCellValue)
      }
    }
  }
  private def getFormulaValue[T](clazz: Class[_]): Option[T] = {
    val book = cell.getSheet.getWorkbook
    val helper = book.getCreationHelper
    val evaluator = helper.createFormulaEvaluator()
    val cellValue = evaluator.evaluate(cell)
    cellValue.getCellType match {
      case Cell.CELL_TYPE_STRING =>
        getStringValue(clazz, cellValue.getStringValue)
      case Cell.CELL_TYPE_NUMERIC =>
        getNumericValue(clazz, cellValue.getNumberValue)
      case Cell.CELL_TYPE_BOOLEAN =>
        Some(cellValue.getBooleanValue.asInstanceOf[T])
      case _ =>
        None
    }
  }

  private def getStringValue[T](clazz: Class[_], strValue: String): Option[T] = {
    if (clazz.equals(classOf[Int])){
      Some(strValue.toInt.asInstanceOf[T])
    } else if (clazz.equals(classOf[Long])){
      Some(strValue.toLong.asInstanceOf[T])
    } else if (clazz.equals(classOf[BigDecimal])) {
      Some(BigDecimal(strValue).asInstanceOf[T])
    } else if (clazz.equals(classOf[String])) {
      Some(strValue.asInstanceOf[T])
    } else {
      throw new java.lang.RuntimeException("Excel String " + ERROR_MESSAGE_STR_NUMERIC)
    }
  }

  private def getNumericValue[T](clazz: Class[_], doubleValue: Double): Option[T] = {
    if (clazz.equals(classOf[Int])){
      Some(doubleValue.toInt.asInstanceOf[T])
    } else if (clazz.equals(classOf[Long])){
      Some(doubleValue.toLong.asInstanceOf[T])
    } else if (clazz.equals(classOf[BigDecimal])) {
      Some(BigDecimal(doubleValue).asInstanceOf[T])
    } else if (clazz.equals(classOf[String])){
      // 小数点が入るのを防ぐためBigDecimalを経由する
      Some(BigDecimal(doubleValue).stripTrailingZeros().toString.asInstanceOf[T])
    } else {
      throw new java.lang.RuntimeException("Excel Numeric " + ERROR_MESSAGE_STR_NUMERIC)
    }
  }
}