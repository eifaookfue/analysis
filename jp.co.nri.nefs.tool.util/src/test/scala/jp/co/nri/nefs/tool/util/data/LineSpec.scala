package jp.co.nri.nefs.tool.util.data

import java.nio.file.{Files, Paths}

import org.scalatest.{BeforeAndAfterAll, FeatureSpec, GivenWhenThen}
import Lines._
import com.typesafe.config.ConfigFactory
import org.apache.poi.ss.usermodel.{Row, Sheet, WorkbookFactory}

class LineSpec extends FeatureSpec with GivenWhenThen with BeforeAndAfterAll {

  private val config = ConfigFactory.load()
  private val baseDir = Paths.get(config.getString(LineSpec.BASE_DIR))
  private val bookName = config.getString(LineSpec.BOOK_NAME)
  //private val url = getClass.getClassLoader.getResource(bookName)
  private val in = Files.newInputStream(baseDir.resolve(bookName))
  private val inBook = WorkbookFactory.create(in)
  val inSheet: Sheet = inBook.getSheet(LineSpec.SHEET_NAME)
  private var rownum = 0
  private var row: Row = _

  override def afterAll(): Unit = {
    in.close()
  }

  feature("The user can define list method in mapping") {

    scenario("a list method is used only 1 time in mapping") {

      Given("list method is placed at the top of the elements in mapping")
      rownum += 3
      val parentLine1 = Line(mapping(
        key(0, 2) -> list(mapping(
          key(0) -> text,
          key(1) -> number
        )(Child1.apply)(Child1.unapply)),
        key(4) -> text,
        key(5) -> number
      )(Parent1.apply)(Parent1.unapply))

      val row1 = inSheet.getRow(rownum)

      When("a bind and get method is called")
      val parent1 = parentLine1.bind(row1).get

      Then("an object can be get.")
      assert(
        parent1
        ===
          Parent1(List(Child1("c1", 1), Child1("c2", 2)), "p1", 3)
      )
      println(s"Got object at is $parent1")

      Given("list method is placed at the middle of the elements in mapping")
      rownum += 2
      val parentLine2 = Line(mapping(
        key(0) -> text,
        key(1, 2) -> list(mapping(
          key(0) -> text,
          key(1) -> number
        )(Child2.apply)(Child2.unapply)),
        key(5) -> number
      )(Parent2.apply)(Parent2.unapply))
      println(s"rownum=$rownum")
      val row2 = inSheet.getRow(rownum)

      When("a bind and get method is called")
      val parent2 = parentLine2.bind(row2).get

      Then("an object can be get.")
      assert(
        parent2
          ===
          Parent2("p1", List(Child2("c1", 1), Child2("c2", 2)), 3)
      )
      println(s"Got object is $parent2")

      Given("list method is placed at the bottom of the elements in mapping")
      rownum += 2
      val parentLine3 = Line(mapping(
        key(0) -> text,
        key(1) -> number,
        key(2, 2) -> list(mapping(
          key(0) -> text,
          key(1) -> number
        )(Child3.apply)(Child3.unapply))
      )(Parent3.apply)(Parent3.unapply))
      val row3 = inSheet.getRow(rownum)

      When("a bind and get method is called")
      val parent3 = parentLine3.bind(row3).get

      Then("big1.bind(row).get")
      assert(
        parent3
          ===
          Parent3("p1", 1, List(Child3("c1", 2), Child3("c2", 3)))
      )
      println(s"Got object is $parent3")

    }

    scenario("list methods are used 2 times in mapping") {

      Given("list method is placed at the top and bottom of the elements in mapping")
      rownum += 3
      val parentLine4 = Line(mapping(
        key(0, 2) -> list(mapping(
          key(0) -> text,
          key(1) -> number
        )(Child4.apply)(Child4.unapply)),
        key(4) -> text,
        key(5, 3) -> list(mapping(
          key(0) -> text,
          key(1) -> number
        )(Child4.apply)(Child4.unapply))
      )(Parent4.apply)(Parent4.unapply))
      val row4 = inSheet.getRow(rownum)

      When("a bind and get method is called")
      val parent4 = parentLine4.bind(row4).get

      Then("an object can be get.")
      assert(
        parent4
          ===
          Parent4(List(Child4("c1", 1), Child4("c2", 2)), "p1", List(Child4("c1", 3), Child4("c2", 4)))
      )
      println(s"Got object is $parent4")

    }

    scenario("a list method is used within a list method") {

      Given("a list method is placed at the bottom of the elements, and another list method is placed in list method")
      rownum += 3
      val parentLine5 = Line(mapping(
        key(0) -> text,
        key(1) -> number,
        key(2, 2) -> list(mapping(
          key(0) -> text,
          key(1) -> number,
          key(2, 2) -> list(mapping(
            key(0) -> text,
            key(1) -> number
          )(GrandChild5.apply)(GrandChild5.unapply))
        )(Child5.apply)(Child5.unapply))
      )(Parent5.apply)(Parent5.unapply))
      val row5 = inSheet.getRow(rownum)

      When("a bind and get method is called")
      val parent5 = parentLine5.bind(row5).get

      Then("an object can be get.")
      assert(
        parent5
          ===
          Parent5("p1", 1,
            List(
              Child5("c1", 2,
                List(GrandChild5("g1", 3), GrandChild5("g2", 4))
              ),
              Child5("c2", 5,
                List(GrandChild5("g3", 6), GrandChild5("g4", 7))
              )
            )
        )
      )
      println(s"Got object is $parent5")

      /*val outPath = Paths.get("C:/tmp/out.xlsx")
      write(parentLine5, Seq(parent), outPath)
      val expected =
        Parent5("p1", 1,
          List(
            Child5("c1", 2,
              List(GrandChild5("g1", 3), GrandChild5("g2", 4))
            ),
            Child5("c2", 5,
              List(GrandChild5("g3", 6), GrandChild5("g4", 7))
            )
          )
        )
      assert(expected === parent)
*/
    }

    scenario("a list method is used on invalid data.") {

      Given("a number is missing on data")
      rownum += 3
      val parentLine6 = Line(mapping(
        key(0) -> text,
        key(1) -> number,
        key(2, 2) -> list(mapping(
          key(0) -> text,
          key(1) -> number
        )(Child6.apply)(Child6.unapply)),
      )(Parent6.apply)(Parent6.unapply))
      val row6 = inSheet.getRow(rownum)

      When("a bind and get method is called")
      val parent6 = parentLine6.bind(row6).get

      Then("the elements of the list are ignored")

      assert(
        parent6
          ===
          Parent6("p1", 1, List(Child6("c2", 3)))
      )
      println(s"Got object is $parent6")

    }


  }

  feature("The user can notice if the data format is invalid.") {

    scenario("String is set at the cell where number is expected.") {

      Given("String(s2) is set at cell where number is expected.")
      rownum += 4
      val parentLine7 = Line(mapping(
        key(0) -> text,
        key(1) -> number
      )(Parent7.apply)(Parent7.unapply))
      val row7 = inSheet.getRow(rownum)

      When("a bind method is called")
      val parendLine7Binded = parentLine7.bind(row7)
      Then("value will return None, and LineError will return  NumberFormatException.")
      assert(parendLine7Binded.value === None)
      assert(parendLine7Binded.errors.head === LineError(1, "error.number"))
    }

  }

  feature("The user can define optional method in mapping.") {

    scenario("a optional method wraps text and number") {
      Given("existing text and existing number")
      rownum += 4
      val parentLine8 = Line(mapping(
        key(0) -> optional(text),
        key(1) -> optional(number)
      )(ParentOp.apply)(ParentOp.unapply))
      val row8 = inSheet.getRow(rownum)

      When("a bind and get method is called")
      val parent8 = parentLine8.bind(row8).get

      Then("existing object can be get.")
      println(s"Got object is $parent8")
      assert(
        parent8
          ===
          ParentOp(Some("p1"), Some(1))
      )

      Given("null and existing number")
      rownum += 2
      val parentLine9 = Line(mapping(
        key(0) -> optional(text),
        key(1) -> optional(number)
      )(Parent9.apply)(Parent9.unapply))
      val row9 = inSheet.getRow(rownum)

      When("a bind and get method is called")
      val parent9 = parentLine9.bind(row9).get

      Then("existing object can be get.")
      println(s"Got object is $parent9")
      assert(
        parent9
          ===
          Parent9(None, Some(1))
      )

      Given("existing text and null")
      rownum += 2
      val parentLine10 = Line(mapping(
        key(0) -> optional(text),
        key(1) -> optional(number)
      )(Parent10.apply)(Parent10.unapply))
      val row10 = inSheet.getRow(rownum)

      When("a bind and get method is called")
      val parent10 = parentLine10.bind(row10).get

      Then("existing object can be get.")
      println(s"Got object is $parent10")
      assert(
        parent10
          ===
          Parent10(Some("p1"), None)
      )
    }

    scenario("a optional method wraps list") {
      Given("existing list")
      rownum += 3
      val parentOpListLine = Line(mapping(
        key(0) -> text,
        key(1, 2) -> optional(list(mapping(
          key(0) -> text,
          key(1) -> number
        )(ChildOpList.apply)(ChildOpList.unapply)))
      )(ParentOpList.apply)(ParentOpList.unapply))
      val row11 = inSheet.getRow(rownum)

      When("a bind and get method is called")
      val parent11 = parentOpListLine.bind(row11).get

      Then("existing object can be get.")
      println(s"Got object is $parent11")
      assert(
        parent11
          ===
          ParentOpList("p1", Some(List(ChildOpList("c1", 1), ChildOpList("c2", 2))))
      )

      Given("null list")
      rownum += 3
      val row12 = inSheet.getRow(rownum)

      When("a bind and get method is called")
      val parent12 = parentOpListLine.bind(row12).get

      Then("existing object can be get.")
      println(s"Got object is $parent12")
      assert(
        parent12
          ===
          ParentOpList("p1", None)
      )
    }

  }

  feature("The user can define bigdecimal method in mapping.") {
    scenario("bigdecimal with no argument") {
      val big1 = Line(mapping(
        key(0) -> bigDecimal
      )(Big1.apply)(Big1.unapply))
      Given(s"integer : [1]")

      rownum += 4
      row = inSheet.getRow(rownum)

      When("a bind and get method is called")
      Then(s"the object got from Line is ${Big1(1)}.")
      assert(
        big1.bind(row).get
          ===
        Big1(1)
      )
      Given("integer with decimal : [1.2]")

      rownum += 2
      row = inSheet.getRow(rownum)

      When("a bind and get method is called")
      Then(s"the object got from Line is ${Big1(1.2)}.")
      assert(
        big1.bind(row).get
          ===
          Big1(1.2)
      )

    }

    scenario("bigdecimal with argument"){
      val big1 = Line(mapping(
        key(0) -> bigDecimal(6, 4)
      )(Big1.apply)(Big1.unapply))
      Given("integer : [12.3456]")

      rownum += 3
      row = inSheet.getRow(rownum)
      big1.bind(row).errors.foreach(println)

      assert(
        big1.bind(row).get
          ===
          Big1(12.3456)
      )
    }
  }

  feature ("The user can use number method in mapping even if the number includes thousand separator") {
    scenario("number includes thousand separator") {
      val int1 = Line(mapping(
        key(0) -> number
      )(Int1.apply)(Int1.unapply))
      Given("integer : [ 12,345,678 ]")

      rownum += 3
      row = inSheet.getRow(rownum)
      assert(
        int1.bind(row).get
        ===
        Int1(12345678)
      )

    }
  }

  feature ("The user can define trimText method in mapping") {

    scenario("string includes spaces.") {

      rownum += 3
      val line = Line(mapping(
        key(0) -> trimText
      )(String1.apply)(String1.unapply))

      row = inSheet.getRow(rownum)
      assert (
        line.bind(row).get
        ===
        String1("abc")
      )
    }
  }

  feature("The user can define transform method in mapping") {

    scenario("number includes spaces.") {
      rownum += 3
      val line = Line(mapping(
        key(0) -> trimText.transform[Int](_.toInt, _.toString)
      )(Int1.apply)(Int1.unapply))
      row = inSheet.getRow(rownum)
      assert(
        line.bind(row).get
        ===
        Int1(123)
      )
    }
  }

}

object LineSpec {
  final val BASE_CONFIG = "LineSpec"
  final val BASE_DIR = BASE_CONFIG + ".base-dir"
  final val BOOK_NAME = BASE_CONFIG + ".book-name"
  final val SHEET_NAME = "sheet1"
}

