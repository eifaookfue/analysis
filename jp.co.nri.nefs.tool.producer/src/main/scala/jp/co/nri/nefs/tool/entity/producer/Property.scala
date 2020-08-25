package jp.co.nri.nefs.tool.entity.producer

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import javax.persistence.Column

import scala.collection.JavaConverters._

case class Property(entityName: String, entityType: Class[_], nullable: Boolean) {
  val paramName: String = Property.toParamName(entityName)
  // 本来paramTypeとしてClassの形式で持ちたかったが、ScalaのClassがJavaのClassにマッピングされてしまうため、Stringで保持
  val (paramTypeName: String, isBigDecimal: Boolean) = Property.toParamTypeName(this)
  val initialName: Option[String] = Property.getInitialName(entityName, nullable)
  val entityFuncName: Option[String] = Property.getEntityFuncName(paramName)
  val paramFuncName: Option[String] = Property.getParamFuncName(entityName)
}

object Property extends LazyLogging{
  import jp.co.nri.nefs.tool.util.config.RichConfig._
  final val BASE_CONFIG = EntityProducer.BASE_CONFIG
  final val PARAM_NAME_MAPPING = BASE_CONFIG + ".param-name-mapping"
  final val INITIAL_MAPPING = BASE_CONFIG + ".initial-mapping"
  final val ENTITY_FUNCTION_MAPPING = BASE_CONFIG + ".entity-function-mapping"
  final val PARAM_FUNC_MAPPING = BASE_CONFIG + ".param-function-mapping"
  private val config = ConfigFactory.load()
  private val paramNameMapping = config.getMapping(PARAM_NAME_MAPPING, logger)
  private val initialValueMapping = config.getMapping(INITIAL_MAPPING, logger)
  private val entityFunctionMapping = config.getMapping(ENTITY_FUNCTION_MAPPING, logger)
  private val paramFuncMapping = config.getMapping(PARAM_FUNC_MAPPING, logger)
  //TODO Tableアノテーションがついているクラスをすべて取得したい
  private val orderClass = Class.forName("jp.co.nri.nefs.oms.entity.property.EOrderProperty")
  val doubleCandidates: List[String] = for {
    e <- orderClass.getEnumConstants.map(_.asInstanceOf[Enum[_]]).toList
    field = e.getDeclaringClass.getDeclaredField(e.name())
    scale = Option (field.getAnnotation(classOf[Column])).map(_.scale()).getOrElse(0)
    if scale > 0
  } yield e.name()


  def toParamName(entityName: String): String = paramNameMapping.getOrElse(entityName, entityName)

  def toParamTypeName(property: Property): (String, Boolean) = {
    var isBigDecimal = false
    val wrapped = if (classOf[Number].isAssignableFrom(property.entityType)) {
      isBigDecimal = true
      if (doubleCandidates.contains(property.entityName)) "Double" else "Int"
    } else if (property.entityType == classOf[Array[java.lang.Integer]]) {
      "Array[Int]"
    } else {
      property.entityType.getSimpleName
    }
    (if (property.nullable) s"Option[$wrapped]" else wrapped, isBigDecimal)
  }

  def getInitialName(entityName: String, nullable: Boolean): Option[String] = initialValueMapping
    .get(entityName).orElse{if (nullable) Some("None") else None}

  def getEntityFuncName(paramName: String): Option[String] = {
    entityFunctionMapping.get(paramName)
  }

  def getParamFuncName(entityName: String): Option[String] = paramFuncMapping.get(entityName)

  private def getMapping[T](configKey: String): Map[String, T] = {
    config.getObjectList(configKey).asScala
      .map(_.unwrapped()).map(_.asScala).map(v => (v.head._1, v.head._2.asInstanceOf[T])).toMap
  }

}
