package jp.co.nri.nefs.tool.entity.producer

import scala.util.Properties

object PropertyProducers {

  final val ENTITY_PUT_VALUE = "entity.putValue"
  final val ENTITY_GET_VALUE = "entity.getValue"
  final val JAVA_BIGDECIMAL = "java.math.BigDecimal"

  def getParamContents(properties: Seq[Property]): String = properties.map(getParamContent)
    .mkString("," + Properties.lineSeparator)

  def getParamContent(property: Property): String = {
    s"\t${property.paramName}: ${property.paramTypeName}" + property.initialName.map(" = " + _).getOrElse("")
  }

  def getPackageContents(propertyClass: Class[_], entityTypes: List[Class[_]]): String = {
    val initials = propertyClass :: entityTypes

    // java.langはimport対象外。ユニークにするためSetに変換
    val uniqueNames = initials.map(_.getName).filter(!_.contains("java.lang")).toSet

    // 同じパッケージのオブジェクトが3つ以上含まれる場合は、すべてインポートする
    val aggNames = for {
      (k, v) <- uniqueNames.groupBy(_.split("\\.").dropRight(1).mkString("."))
      if v.size >= 3
    } yield k

    val packageNames = uniqueNames.filter(s => aggNames.forall(!s.contains(_))).toBuffer
    packageNames ++= aggNames.map(_ + "._")

    packageNames.map("import " + _).sorted.mkString(Properties.lineSeparator)

  }

  def getEntitySetting(propertyClassName: String, property: Property): String = {
    property.entityFuncName match {
      case Some(entityFuncName) => s"\t$ENTITY_PUT_VALUE($propertyClassName.${property.entityName}, $entityFuncName)"
      case None =>
        if (property.nullable) {
          val base = s"\t${property.paramName}.foreach($ENTITY_PUT_VALUE($propertyClassName.${property.entityName}, "
          val additional = if (property.isBigDecimal) s"$JAVA_BIGDECIMAL.valueOf[_])" else "_"
          base + additional
        } else {
          val base = s"\t$ENTITY_PUT_VALUE($propertyClassName.${property.entityName}, "
          val additional = if (property.isBigDecimal) {
            s"$JAVA_BIGDECIMAL.valueOf[${property.paramName}])"
          } else {s"${property.paramName})"}
          base + additional
        }
    }

  }

  def getEntitySettings(propertyClassName: String, properties: Seq[Property]): String = {
    properties.map(getEntitySetting(propertyClassName, _)).mkString(Properties.lineSeparator)
  }

  def getParamSetting(propertyClassName: String, property: Property): String = {
    property.paramFuncName match {
      case Some(funcName) => s"\t\t\t$funcName"
      case None =>
        if (property.nullable) {
          s"\t\t\tOption($ENTITY_GET_VALUE($propertyClassName.${property.entityName})).orElse(None)"
        } else {
          s"\t\t\t$ENTITY_GET_VALUE($propertyClassName.${property.entityName})"
        }
    }
  }

  def getParamSettings(paramClassName: String, propertyClassName: String, properties: Seq[Property]): String = {
    val sb = new StringBuilder
    sb.append(s"\t\t$paramClassName(")
    sb.append(Properties.lineSeparator)
    sb.append(properties.map(getParamSetting(propertyClassName, _))
      .mkString("," + Properties.lineSeparator))
    sb.append(Properties.lineSeparator)
    sb.append("\t\t)")
    sb.toString()
  }
}

