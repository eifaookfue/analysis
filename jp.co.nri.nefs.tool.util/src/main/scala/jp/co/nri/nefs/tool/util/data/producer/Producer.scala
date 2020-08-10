package jp.co.nri.nefs.tool.util.data.producer

class Producer(number: Int) {
  val numbers: Seq[Int] = 1 to number
  val commaer: Seq[String] => String = _.mkString(", ")

  def produce(mapper: Int => String, finalizer: Seq[String] => String = commaer): String = finalizer(numbers.map(mapper))

  def one(f1: String => String, f2: String => String)(value: String): String = if (number == 1) f1(value) else f2(value)
}
