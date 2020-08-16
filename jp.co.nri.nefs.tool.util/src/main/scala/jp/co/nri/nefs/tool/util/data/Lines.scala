package jp.co.nri.nefs.tool.util.data

import java.nio.file.{Files, Path}

import jp.co.nri.nefs.tool.util.data.format._
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import scala.reflect.runtime.universe._

object Lines {

  def mapping[R, A1](a1: (Key, Mapping[A1]))(apply: A1 => R)(unapply: R => Option[A1])(implicit evidence: TypeTag[R]): Mapping[R] = {
    new ObjectMapping1(apply, unapply, a1)
  }

  def mapping[R, A1, A2](a1: (Key, Mapping[A1]), a2: (Key, Mapping[A2]))(apply: (A1, A2) => R)(unapply: R => Option[(A1, A2)])(implicit evidence: TypeTag[R]): Mapping[R] = {
    new ObjectMapping2(apply, unapply, a1, a2)
  }

  def mapping[R, A1, A2, A3](a1: (Key, Mapping[A1]), a2: (Key, Mapping[A2]), a3: (Key, Mapping[A3]))(apply: (A1, A2, A3) => R)(unapply: R => Option[(A1, A2, A3)])(implicit evidence: TypeTag[R]): Mapping[R] = {
    new ObjectMapping3(apply, unapply, a1, a2, a3)
  }

  def mapping[R, A1, A2, A3, A4](a1: (Key, Mapping[A1]), a2: (Key, Mapping[A2]), a3: (Key, Mapping[A3]), a4: (Key, Mapping[A4]))(apply: (A1, A2, A3, A4) => R)(unapply: R => Option[(A1, A2, A3, A4)])(implicit evidence: TypeTag[R]): Mapping[R] = {
    new ObjectMapping4(apply, unapply, a1, a2, a3, a4)
  }

  def mapping[R, A1, A2, A3, A4, A5](a1: (Key, Mapping[A1]), a2: (Key, Mapping[A2]), a3: (Key, Mapping[A3]), a4: (Key, Mapping[A4]), a5: (Key, Mapping[A5]))(apply: (A1, A2, A3, A4, A5) => R)(unapply: R => Option[(A1, A2, A3, A4, A5)])(implicit evidence: TypeTag[R]): Mapping[R] = {
    new ObjectMapping5(apply, unapply, a1, a2, a3, a4, a5)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6](a1: (Key, Mapping[A1]), a2: (Key, Mapping[A2]), a3: (Key, Mapping[A3]), a4: (Key, Mapping[A4]), a5: (Key, Mapping[A5]), a6: (Key, Mapping[A6]))(apply: (A1, A2, A3, A4, A5, A6) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6)])(implicit evidence: TypeTag[R]): Mapping[R] = {
    new ObjectMapping6(apply, unapply, a1, a2, a3, a4, a5, a6)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7](a1: (Key, Mapping[A1]), a2: (Key, Mapping[A2]), a3: (Key, Mapping[A3]), a4: (Key, Mapping[A4]), a5: (Key, Mapping[A5]), a6: (Key, Mapping[A6]), a7: (Key, Mapping[A7]))(apply: (A1, A2, A3, A4, A5, A6, A7) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7)])(implicit evidence: TypeTag[R]): Mapping[R] = {
    new ObjectMapping7(apply, unapply, a1, a2, a3, a4, a5, a6, a7)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7, A8](a1: (Key, Mapping[A1]), a2: (Key, Mapping[A2]), a3: (Key, Mapping[A3]), a4: (Key, Mapping[A4]), a5: (Key, Mapping[A5]), a6: (Key, Mapping[A6]), a7: (Key, Mapping[A7]), a8: (Key, Mapping[A8]))(apply: (A1, A2, A3, A4, A5, A6, A7, A8) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8)])(implicit evidence: TypeTag[R]): Mapping[R] = {
    new ObjectMapping8(apply, unapply, a1, a2, a3, a4, a5, a6, a7, a8)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7, A8, A9](a1: (Key, Mapping[A1]), a2: (Key, Mapping[A2]), a3: (Key, Mapping[A3]), a4: (Key, Mapping[A4]), a5: (Key, Mapping[A5]), a6: (Key, Mapping[A6]), a7: (Key, Mapping[A7]), a8: (Key, Mapping[A8]), a9: (Key, Mapping[A9]))(apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9)])(implicit evidence: TypeTag[R]): Mapping[R] = {
    new ObjectMapping9(apply, unapply, a1, a2, a3, a4, a5, a6, a7, a8, a9)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10](a1: (Key, Mapping[A1]), a2: (Key, Mapping[A2]), a3: (Key, Mapping[A3]), a4: (Key, Mapping[A4]), a5: (Key, Mapping[A5]), a6: (Key, Mapping[A6]), a7: (Key, Mapping[A7]), a8: (Key, Mapping[A8]), a9: (Key, Mapping[A9]), a10: (Key, Mapping[A10]))(apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10)])(implicit evidence: TypeTag[R]): Mapping[R] = {
    new ObjectMapping10(apply, unapply, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11](a1: (Key, Mapping[A1]), a2: (Key, Mapping[A2]), a3: (Key, Mapping[A3]), a4: (Key, Mapping[A4]), a5: (Key, Mapping[A5]), a6: (Key, Mapping[A6]), a7: (Key, Mapping[A7]), a8: (Key, Mapping[A8]), a9: (Key, Mapping[A9]), a10: (Key, Mapping[A10]), a11: (Key, Mapping[A11]))(apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11)])(implicit evidence: TypeTag[R]): Mapping[R] = {
    new ObjectMapping11(apply, unapply, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12](a1: (Key, Mapping[A1]), a2: (Key, Mapping[A2]), a3: (Key, Mapping[A3]), a4: (Key, Mapping[A4]), a5: (Key, Mapping[A5]), a6: (Key, Mapping[A6]), a7: (Key, Mapping[A7]), a8: (Key, Mapping[A8]), a9: (Key, Mapping[A9]), a10: (Key, Mapping[A10]), a11: (Key, Mapping[A11]), a12: (Key, Mapping[A12]))(apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12)])(implicit evidence: TypeTag[R]): Mapping[R] = {
    new ObjectMapping12(apply, unapply, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13](a1: (Key, Mapping[A1]), a2: (Key, Mapping[A2]), a3: (Key, Mapping[A3]), a4: (Key, Mapping[A4]), a5: (Key, Mapping[A5]), a6: (Key, Mapping[A6]), a7: (Key, Mapping[A7]), a8: (Key, Mapping[A8]), a9: (Key, Mapping[A9]), a10: (Key, Mapping[A10]), a11: (Key, Mapping[A11]), a12: (Key, Mapping[A12]), a13: (Key, Mapping[A13]))(apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13)])(implicit evidence: TypeTag[R]): Mapping[R] = {
    new ObjectMapping13(apply, unapply, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14](a1: (Key, Mapping[A1]), a2: (Key, Mapping[A2]), a3: (Key, Mapping[A3]), a4: (Key, Mapping[A4]), a5: (Key, Mapping[A5]), a6: (Key, Mapping[A6]), a7: (Key, Mapping[A7]), a8: (Key, Mapping[A8]), a9: (Key, Mapping[A9]), a10: (Key, Mapping[A10]), a11: (Key, Mapping[A11]), a12: (Key, Mapping[A12]), a13: (Key, Mapping[A13]), a14: (Key, Mapping[A14]))(apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14)])(implicit evidence: TypeTag[R]): Mapping[R] = {
    new ObjectMapping14(apply, unapply, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15](a1: (Key, Mapping[A1]), a2: (Key, Mapping[A2]), a3: (Key, Mapping[A3]), a4: (Key, Mapping[A4]), a5: (Key, Mapping[A5]), a6: (Key, Mapping[A6]), a7: (Key, Mapping[A7]), a8: (Key, Mapping[A8]), a9: (Key, Mapping[A9]), a10: (Key, Mapping[A10]), a11: (Key, Mapping[A11]), a12: (Key, Mapping[A12]), a13: (Key, Mapping[A13]), a14: (Key, Mapping[A14]), a15: (Key, Mapping[A15]))(apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15)])(implicit evidence: TypeTag[R]): Mapping[R] = {
    new ObjectMapping15(apply, unapply, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16](a1: (Key, Mapping[A1]), a2: (Key, Mapping[A2]), a3: (Key, Mapping[A3]), a4: (Key, Mapping[A4]), a5: (Key, Mapping[A5]), a6: (Key, Mapping[A6]), a7: (Key, Mapping[A7]), a8: (Key, Mapping[A8]), a9: (Key, Mapping[A9]), a10: (Key, Mapping[A10]), a11: (Key, Mapping[A11]), a12: (Key, Mapping[A12]), a13: (Key, Mapping[A13]), a14: (Key, Mapping[A14]), a15: (Key, Mapping[A15]), a16: (Key, Mapping[A16]))(apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16)])(implicit evidence: TypeTag[R]): Mapping[R] = {
    new ObjectMapping16(apply, unapply, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17](a1: (Key, Mapping[A1]), a2: (Key, Mapping[A2]), a3: (Key, Mapping[A3]), a4: (Key, Mapping[A4]), a5: (Key, Mapping[A5]), a6: (Key, Mapping[A6]), a7: (Key, Mapping[A7]), a8: (Key, Mapping[A8]), a9: (Key, Mapping[A9]), a10: (Key, Mapping[A10]), a11: (Key, Mapping[A11]), a12: (Key, Mapping[A12]), a13: (Key, Mapping[A13]), a14: (Key, Mapping[A14]), a15: (Key, Mapping[A15]), a16: (Key, Mapping[A16]), a17: (Key, Mapping[A17]))(apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17)])(implicit evidence: TypeTag[R]): Mapping[R] = {
    new ObjectMapping17(apply, unapply, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18](a1: (Key, Mapping[A1]), a2: (Key, Mapping[A2]), a3: (Key, Mapping[A3]), a4: (Key, Mapping[A4]), a5: (Key, Mapping[A5]), a6: (Key, Mapping[A6]), a7: (Key, Mapping[A7]), a8: (Key, Mapping[A8]), a9: (Key, Mapping[A9]), a10: (Key, Mapping[A10]), a11: (Key, Mapping[A11]), a12: (Key, Mapping[A12]), a13: (Key, Mapping[A13]), a14: (Key, Mapping[A14]), a15: (Key, Mapping[A15]), a16: (Key, Mapping[A16]), a17: (Key, Mapping[A17]), a18: (Key, Mapping[A18]))(apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18)])(implicit evidence: TypeTag[R]): Mapping[R] = {
    new ObjectMapping18(apply, unapply, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19](a1: (Key, Mapping[A1]), a2: (Key, Mapping[A2]), a3: (Key, Mapping[A3]), a4: (Key, Mapping[A4]), a5: (Key, Mapping[A5]), a6: (Key, Mapping[A6]), a7: (Key, Mapping[A7]), a8: (Key, Mapping[A8]), a9: (Key, Mapping[A9]), a10: (Key, Mapping[A10]), a11: (Key, Mapping[A11]), a12: (Key, Mapping[A12]), a13: (Key, Mapping[A13]), a14: (Key, Mapping[A14]), a15: (Key, Mapping[A15]), a16: (Key, Mapping[A16]), a17: (Key, Mapping[A17]), a18: (Key, Mapping[A18]), a19: (Key, Mapping[A19]))(apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19)])(implicit evidence: TypeTag[R]): Mapping[R] = {
    new ObjectMapping19(apply, unapply, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20](a1: (Key, Mapping[A1]), a2: (Key, Mapping[A2]), a3: (Key, Mapping[A3]), a4: (Key, Mapping[A4]), a5: (Key, Mapping[A5]), a6: (Key, Mapping[A6]), a7: (Key, Mapping[A7]), a8: (Key, Mapping[A8]), a9: (Key, Mapping[A9]), a10: (Key, Mapping[A10]), a11: (Key, Mapping[A11]), a12: (Key, Mapping[A12]), a13: (Key, Mapping[A13]), a14: (Key, Mapping[A14]), a15: (Key, Mapping[A15]), a16: (Key, Mapping[A16]), a17: (Key, Mapping[A17]), a18: (Key, Mapping[A18]), a19: (Key, Mapping[A19]), a20: (Key, Mapping[A20]))(apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20)])(implicit evidence: TypeTag[R]): Mapping[R] = {
    new ObjectMapping20(apply, unapply, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19, a20)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21](a1: (Key, Mapping[A1]), a2: (Key, Mapping[A2]), a3: (Key, Mapping[A3]), a4: (Key, Mapping[A4]), a5: (Key, Mapping[A5]), a6: (Key, Mapping[A6]), a7: (Key, Mapping[A7]), a8: (Key, Mapping[A8]), a9: (Key, Mapping[A9]), a10: (Key, Mapping[A10]), a11: (Key, Mapping[A11]), a12: (Key, Mapping[A12]), a13: (Key, Mapping[A13]), a14: (Key, Mapping[A14]), a15: (Key, Mapping[A15]), a16: (Key, Mapping[A16]), a17: (Key, Mapping[A17]), a18: (Key, Mapping[A18]), a19: (Key, Mapping[A19]), a20: (Key, Mapping[A20]), a21: (Key, Mapping[A21]))(apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21)])(implicit evidence: TypeTag[R]): Mapping[R] = {
    new ObjectMapping21(apply, unapply, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19, a20, a21)
  }

  def mapping[R, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, A22](a1: (Key, Mapping[A1]), a2: (Key, Mapping[A2]), a3: (Key, Mapping[A3]), a4: (Key, Mapping[A4]), a5: (Key, Mapping[A5]), a6: (Key, Mapping[A6]), a7: (Key, Mapping[A7]), a8: (Key, Mapping[A8]), a9: (Key, Mapping[A9]), a10: (Key, Mapping[A10]), a11: (Key, Mapping[A11]), a12: (Key, Mapping[A12]), a13: (Key, Mapping[A13]), a14: (Key, Mapping[A14]), a15: (Key, Mapping[A15]), a16: (Key, Mapping[A16]), a17: (Key, Mapping[A17]), a18: (Key, Mapping[A18]), a19: (Key, Mapping[A19]), a20: (Key, Mapping[A20]), a21: (Key, Mapping[A21]), a22: (Key, Mapping[A22]))(apply: (A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, A22) => R)(unapply: R => Option[(A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, A21, A22)])(implicit evidence: TypeTag[R]): Mapping[R] = {
    new ObjectMapping22(apply, unapply, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19, a20, a21, a22)
  }

  def list[A](mapping: Mapping[A])(implicit evidence: TypeTag[A]): Mapping[List[A]] = RepeatedMapping(mapping)

  def key(index: Int, count: Int = 1): Key = Key(index, count)

  import Formats._

  val text: Mapping[String] = of[String]

  val number: Mapping[Int] = of[Int]

  def of[T](implicit binder: Formatter[T]): FieldMapping[T] = FieldMapping[T]()(binder)

  def write[T: TypeTag](line: Line[T], values: Seq[T], path: Path, sheetName: String = "sheet1",
               start: Int = 0, header: Boolean = true, headers: Option[Seq[String]] = None): Unit ={

    import jp.co.nri.nefs.tool.util.RichFiles._

    assert(path.toString.extension == "xlsx", s"path should have xlsx extension.")

    val (book, in) = if (Files.exists(path)) {
      val in = Files.newInputStream(path)
      (WorkbookFactory.create(in), Some(in))
    } else {
      (new XSSFWorkbook(), None)
    }
    val out = Files.newOutputStream(path)
    val sheet = Option(book.getSheet(sheetName)).getOrElse(book.createSheet(sheetName))
    val start2 = if (header) {
      val row = sheet.createRow(start)
      for ((param, index) <- line.mapping.paramNames.zipWithIndex) row.createCell(index).setCellValue(param)
      1
    } else 0
    for {
      (value, index) <- values.zipWithIndex
      row = sheet.createRow(index + start2)
    } line.mapping.unbind(value, row)
    // auto width
    line.mapping.paramNames.indices.foreach(sheet.autoSizeColumn)
    try {
      book.write(out)
    } catch {
      case e: Exception => println(e)
    } finally {
      in.foreach(_.close())
      out.close()
    }


  }


}
