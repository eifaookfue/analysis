package jp.co.nri.nefs.tool.util.data

import java.nio.file.{Files, Path, Paths}

import org.scalatest.{FeatureSpec, GivenWhenThen}
import Lines._
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.poi.ss.usermodel.WorkbookFactory

class LineSpec extends FeatureSpec with GivenWhenThen {

  private val config = ConfigFactory.load()
  private val baseDir = Paths.get(config.getString(LineSpec.BASE_DIR))
  private val bookName = config.getString(LineSpec.BOOK_NAME)
  private val url = getClass.getClassLoader.getResource(bookName)
  private val in = Files.newInputStream(Paths.get(url.toURI))
  private val inBook = WorkbookFactory.create(in)


  feature("The user can define list method in mapping") {

    scenario("a list method is used only 1 time in mapping") {
      Given("list method is placed at the top of the elements in mapping")
      val CASE1 = "case1"
      case class Child(c1: String, i2: Int)
      case class Parent(l1: List[Child], p2: String, i3: Int)

      val parentLine = Line(mapping(
        key(0, 2) -> list(mapping(
          key(0) -> text,
          key(1) -> number
        )(Child.apply)(Child.unapply)),
        key(1) -> text,
        key(2) -> number
      )(Parent.apply)(Parent.unapply))
      val inSheet = inBook.getSheet(CASE1)
      val row = inSheet.getRow(0)

      When("When a bind method is called")
      val parent = parentLine.bind(row).get

      Then("A object can be get.")
      println(parent)

    }

    scenario("a list method is used 2 time in mapping") {

    }

    scenario("list has 2 layers") {

      val parentLine = Line(mapping(
        key(0) -> text,
        key(1) -> number,
        key(2, 2) -> list(mapping(
          key(0) -> text,
          key(1) -> number,
          key(2, 2) -> list(mapping(
            key(0) -> text,
            key(1) -> number
          )(GrandChild.apply)(GrandChild.unapply))
        )(Child.apply)(Child.unapply))
      )(Parent.apply)(Parent.unapply))



      Files.createDirectories(baseDir)
/*
      val out = Files.newOutputStream(baseDir.resolve(bookName))
      val outBook = new XSSFWorkbook()

      val sheet = outBook.createSheet()
      val row = sheet.createRow(0)
      row.createCell(0).setCellValue("p1")
      row.createCell(1).setCellValue(2)
      row.createCell(2).setCellValue("c1")
      row.createCell(3).setCellValue(3)
      row.createCell(4).setCellValue("g1")
      row.createCell(5).setCellValue(4)
      row.createCell(6).setCellValue("g2")
      row.createCell(7).setCellValue(5)
      row.createCell(8).setCellValue("c2")
      row.createCell(9).setCellValue(6)
      row.createCell(10).setCellValue("g3")
      row.createCell(11).setCellValue(7)
      row.createCell(12).setCellValue("g4")
      row.createCell(13).setCellValue(8)

      outBook.write(out)
      out.close()
*/

      val url = getClass.getClassLoader.getResource(bookName)
      val in = Files.newInputStream(Paths.get(url.toURI))
      val inBook = WorkbookFactory.create(in)
      val inSheet = inBook.getSheetAt(0)
      val row = inSheet.getRow(0)
      val parent = parentLine.bind(row).get
      println(parent)

      println(parentLine.mapping.paramNames)
      val outPath = Paths.get("C:/tmp/out.xlsx")
      write(parentLine, Seq(parent), outPath)
      val expected =
        Parent("p1", 1,
          List(
            Child("c1", 2,
              List(GrandChild("g1", 3), GrandChild("g2", 4))
            ),
            Child("c2", 5,
              List(GrandChild("g3", 6), GrandChild("g4", 7))
            )
          )
        )
      assert(expected === parent)

    }
  }
}

object LineSpec {
  final val BASE_CONFIG = "LineSpec"
  final val BASE_DIR = BASE_CONFIG + ".base-dir"
  final val BOOK_NAME = BASE_CONFIG + ".book-name"
}

case class Parent(s1: String, i2: Int, l3: List[Child])
case class Child(s1: String, i2: Int, l3: List[GrandChild])
case class GrandChild(s1: String, i2: Int)