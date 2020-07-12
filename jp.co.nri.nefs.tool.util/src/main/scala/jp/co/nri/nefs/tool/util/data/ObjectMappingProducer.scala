package jp.co.nri.nefs.tool.util.data

import scala.util.Properties

/*
val field1: Mapping[A1] = f1._2.withPrefix(f1._1)
  val field2: Mapping[A2] = f2._2.withPrefix(f2._1)



}
 */

object ObjectMappingProducer {

  def main(args: Array[String]): Unit = {
    for (i <- 1 to 22){
      print(s"class ObjectMapping$i[R, ${aSeq(i)}](apply: ${aTuple(i)} => R, unapply: R => Option[${aTuple(i)}], ")
      print(fMappingSeq(i))
      print(", index: Int = 0)")
      println
      println("\textends Mapping[R] with ObjectMapping {")
      println
      println(fieldSeq(i))
      println
      println("\toverride def bind(row: Row): Either[Seq[LineError], R] = {")
      println(s"\t\tmerge(${mergeSeq(i)}) match {")
      println("\t\t\tcase Left(errors) => Left(errors)")
      println(s"\t\t\tcase Right(values) => Right(apply(${valSeq(i)}))")
      println("\t\t}")
      println("\t}")
      println
      println("\toverride def unbind(value: R, row: Row): Unit = {")
      println(s"\t\tval ${vTuple(i)} = unapply(value).get")
      println(fieldUnbindSeq(i))
      println("\t}")
      println
      println(s"\toverride def withPrefix(prefix: Int): ObjectMapping$i[R, ${aSeq(i)}] = {")
      println(s"\t\tnew ObjectMapping$i(apply, unapply, ${fSeq(i)}, prefix)")
      println("\t}")
      println
      println("}")
      println
    }
  }

  def fieldSeq(number: Int): String = {
    (1 to number).map(i => s"\tval field$i: Mapping[A$i] = f$i._2.withPrefix(f$i._1)").mkString(Properties.lineSeparator)
  }

  def mergeSeq(number: Int): String = {
    (1 to number).map(i => s"field$i.bind(row)").mkString(", ")
  }

  def fieldUnbindSeq(number: Int): String = {
    (1 to number).map(i => s"\t\tfield$i.unbind(v$i, row)").mkString(Properties.lineSeparator)
  }

  def valSeq(number: Int): String = {
    (1 to number).map(i => s"values(${i-1}).asInstanceOf[A$i]").mkString(", ")
  }

  def vTuple(number: Int): String = {
    "(" + (1 to number).map("v" + _).mkString(", ") + ")"
  }

  def fMappingSeq(number: Int): String = {
    (1 to number).map(i => s"f$i: (Int, Mapping[A$i])").mkString(", ")
  }

  def fSeq(number: Int): String = {
    (1 to number).map("f" + _).mkString(", ")
  }

  def aSeq(number: Int): String = {
    (1 to number).map("A" + _).mkString(", ")
  }

  def aTuple(number: Int): String = if (number > 1) "(" + aSeq(number) + ")" else aSeq(number)
}
