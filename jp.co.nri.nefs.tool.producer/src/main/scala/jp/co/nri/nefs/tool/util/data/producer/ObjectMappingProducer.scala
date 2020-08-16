package jp.co.nri.nefs.tool.util.data.producer

import com.typesafe.config.{Config, ConfigFactory}

import scala.util.Properties

object ObjectMappingProducer {

  final val BASE_CONFIG = "ObjectMappingProducer"
  final val OBJECT_MAPPING_START = BASE_CONFIG + ".object-mapping-start"
  final val OBJECT_MAPPING_END = BASE_CONFIG + ".object-mapping-end"
  val config: Config = ConfigFactory.load()
  val objectMappingStart: Int = config.getInt(OBJECT_MAPPING_START)
  val objectMappingEnd: Int = config.getInt(OBJECT_MAPPING_END)


  def main(args: Array[String]): Unit = {

    for (i <- objectMappingStart to objectMappingEnd){

      val p = new Producer(i)
      val aSeq = p.produce("A" + _)
      val aTuple = p.one(s => s, "(" + _ + ")")(aSeq)
      val fMappingSeq = p.produce(i => s"f$i: (Key, Mapping[A$i])")
      val lineFinalizer: Seq[String] => String = _.mkString(Properties.lineSeparator)
      val fieldSeq = p.produce({ i =>
        val params = if (i == 1) {
          "params.head"
        } else s"params(${i-1})"
        s"\tval field$i: Mapping[A$i] = f$i._2.withKey(f$i._1).withKey(key).withParamName($params)"
      }, lineFinalizer)
      val mergeSeq = p.produce(i => s"field$i.bind(row)")
      val valSeq = p.produce{i =>
        if (i == 1) {
          s"values.head.asInstanceOf[A$i]"
        } else {
          s"values(${i-1}).asInstanceOf[A$i]"
        }
      }
      val vTuple = p.one(i => i, "(" + _ + ")")(p.produce(i => s"v$i"))
      val fieldUnbindSeq = p.produce(i => s"\t\tfield$i.unbind(v$i, row)", lineFinalizer)
      val fSeq = p.produce("f" + _)
      val fieldSeq2 = p.produce("field" + _)

      print(s"class ObjectMapping$i[R, $aSeq](apply: $aTuple => R, unapply: R => Option[$aTuple], ")
      print(fMappingSeq)
      println(", val key: Key = null, val paramName: String = null)(implicit evidence: TypeTag[R])")
      println("\textends Mapping[R] with ObjectMapping {")
      println
      println("\tprivate val params = paramNames(evidence.tpe)")
      println(fieldSeq)
      println
      println("\toverride def bind(row: Row): Either[Seq[LineError], R] = {")
      println(s"\t\tmerge($mergeSeq) match {")
      println("\t\t\tcase Left(errors) => Left(errors)")
      println(s"\t\t\tcase Right(values) => Right(apply($valSeq))")
      println("\t\t}")
      println("\t}")
      println
      println("\toverride def unbind(value: R, row: Row): Unit = {")
      println(s"\t\tval $vTuple = unapply(value).get")
      println(fieldUnbindSeq)
      println("\t}")
      println
      println(s"\toverride def withKey(key: Key): ObjectMapping$i[R, $aSeq] = addKey(key).map(newKey =>")
      println(s"\t\tnew ObjectMapping$i(apply, unapply, $fSeq, newKey)")
      println(s"\t).getOrElse(this)")
      println
      println(s"\toverride def withParamName(paramName: String): Mapping[R] = {")
      println(s"\t\tnew ObjectMapping$i(apply, unapply, $fSeq, key, paramName)")
      println("\t}")
      println
      println(s"\toverride def paramNames: Seq[String] = Seq($fieldSeq2).flatMap(_.paramNames)")
      println
      println("}")
      println
    }
  }

}
