package jp.co.nri.nefs.tool.util.data

object LinesProducer {
  def main(args: Array[String]): Unit = {
    for (i <- 1 to 22){
      println(s"def mapping[R, ${aSeq(i)}](${mappingSeq(i)})(apply: ${aTuple(i)} => R)(unapply: R => Option[${aTuple(i)}]): Mapping[R] = {")
      println(s"\tnew ObjectMapping$i(apply, unapply, ${smallAseq(i)})")
      println("}")
      println
    }
  }

  def smallAseq(number: Int): String = {
    (1 to number).map("a" + _).mkString(", ")
  }

  def aSeq(number: Int): String = {
    (1 to number).map("A" + _).mkString(", ")
  }

  def aTuple(number: Int): String = if (number > 1) "(" + aSeq(number) + ")" else aSeq(number)

  def mappingSeq(number: Int): String = {
    (1 to number).map(i => s"a$i: (Int, Mapping[A$i])").mkString(", ")
  }

}
