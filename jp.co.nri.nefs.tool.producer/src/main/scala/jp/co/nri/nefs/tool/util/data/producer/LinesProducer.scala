package jp.co.nri.nefs.tool.util.data.producer

import com.typesafe.config.{Config, ConfigFactory}

object LinesProducer {

  final val BASE_CONFIG = "LinesProducer"
  final val LINES_START = BASE_CONFIG + ".lines-start"
  final val LINES_END = BASE_CONFIG + ".lines-end"
  val config: Config = ConfigFactory.load()
  val linesStart: Int = config.getInt(LINES_START)
  val linesEnd: Int = config.getInt(LINES_END)

  def main(args: Array[String]): Unit = {

    for (i <- linesStart to linesEnd){
      val p = new Producer(i)
      val aSeq = p.produce("A" + _)
      val mappingSeq = p.produce(i => s"a$i: (Key, Mapping[A$i])")
      val aTuple = p.one(i => i, "(" + _ + ")")(aSeq)
      val aSeq2 = p.produce("a" + _)

      println(s"def mapping[R, $aSeq]($mappingSeq)(apply: $aTuple => R)(unapply: R => Option[$aTuple])(implicit evidence: TypeTag[R]): Mapping[R] = {")
      println(s"\tnew ObjectMapping$i(apply, unapply, $aSeq2)")
      println("}")
      println
    }
  }

}
