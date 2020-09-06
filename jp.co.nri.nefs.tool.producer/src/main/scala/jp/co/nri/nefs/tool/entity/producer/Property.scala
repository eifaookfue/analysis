package jp.co.nri.nefs.tool.entity.producer

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import javax.persistence.Column

case class Property(entityName: String, entityType: Class[_], nullable: Boolean) {

  import CONVERT_MODE._

  val convertMode: CONVERT_MODE = (entityName, entityType) match {
    case ("INSTRUMENT_ID", _) => SYMBOL
    case (_, t) if classOf[Number].isAssignableFrom(t) => BIGDECIMAL
    case _ => NORMAL
  }

  val paramName: String = convertMode match {
    case SYMBOL => "SYMBOL"
    case BIGDECIMAL | NORMAL => entityName
  }

  // 本来paramTypeとしてClassの形式で持ちたかったが、ScalaのClassがJavaのClassにマッピングされてしまうため、Stringで保持
  val paramTypeName: String = {
    val wrapped = convertMode match {
      case BIGDECIMAL => "BigDecimal"
      case SYMBOL | NORMAL => entityType.getSimpleName
    }
    if (nullable) s"Option[$wrapped]" else wrapped
  }

}

object Property extends LazyLogging{
  //TODO Tableアノテーションがついているクラスをすべて取得したい
  private val orderClass = Class.forName("jp.co.nri.nefs.oms.entity.property.EOrderProperty")
  val doubleCandidates: List[String] = for {
    e <- orderClass.getEnumConstants.map(_.asInstanceOf[Enum[_]]).toList
    field = e.getDeclaringClass.getDeclaredField(e.name())
    scale = Option (field.getAnnotation(classOf[Column])).map(_.scale()).getOrElse(0)
    if scale > 0
  } yield e.name()

}

sealed trait CONVERT_MODE

object CONVERT_MODE {
  case object NORMAL extends CONVERT_MODE
  case object BIGDECIMAL extends CONVERT_MODE
  case object SYMBOL extends CONVERT_MODE
}