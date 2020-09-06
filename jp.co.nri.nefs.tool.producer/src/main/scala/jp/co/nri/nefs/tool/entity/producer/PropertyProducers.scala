package jp.co.nri.nefs.tool.entity.producer

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import jp.co.nri.nefs.common.model.entity.{DefaultEntity, IEntity}

import scala.util.Properties

object PropertyProducers extends LazyLogging {

  import jp.co.nri.nefs.tool.util.config.RichConfig._
  import CONVERT_MODE._

  final val ENTITY_PUT_VALUE = "entity.putValue"
  final val ENTITY_GET_VALUE = "entity.getValue"
  final val JAVA_BIGDECIMAL = "java.math.BigDecimal"
  final val BASE_CONFIG = ProxyProducer.BASE_CONFIG
  final val PACKAGE_SHORTNAME_MAPPING = BASE_CONFIG + ".package-shortname-mapping"
  final val INITIAL_MAPPING = BASE_CONFIG + ".initial-mapping"
  final val IMPORT_EXCLUDE_PACKAGE_LIST = BASE_CONFIG + ".import-exclude-package-list"
  final val IMPORT_EXCLUDE_CLASS_LIST = BASE_CONFIG + ".import-exclude-class-list"
  private val config = ConfigFactory.load()
  private val packageShortNameMapping: Map[String, String] = config.getMapping(PACKAGE_SHORTNAME_MAPPING, logger)
  private val initialValueMapping: Map[String, String] = config.getMapping(INITIAL_MAPPING, logger)
  private val importExcludePackageList: List[String] = config.getStringList(IMPORT_EXCLUDE_PACKAGE_LIST, logger)
  private val importExcludeClassList: List[String] = config.getStringList(IMPORT_EXCLUDE_CLASS_LIST, logger)

  def entityValueAndPackage(p: Property): (String, Option[String]) = p.convertMode match {
    case SYMBOL => (s"${p.paramName}.toInst", Some("jp.co.nri.nefs.tool.ViewCodeWrapper._"))
    case BIGDECIMAL => (s"${p.paramName}.underlying", None)
    case NORMAL => (s"${p.paramName}", None)
  }

  def createEntitySetting(propertyClass: Class[_], p: Property): String = {
    val baseName = packageShortName(propertyClass).getOrElse(propertyClass.getSimpleName)
    if (p.nullable)
      s"\t${p.entityName}.foreach($ENTITY_PUT_VALUE($baseName.${p.entityName}, ${entityValueAndPackage(p)._1.replace(p.entityName, "_")}))"
    else
      s"\t$ENTITY_PUT_VALUE($baseName.${p.entityName}, ${entityValueAndPackage(p)._1})"
  }

  def createEntitySettings(propertyClass: Class[_], properties: Seq[Property]): String = {
    properties.map(p => createEntitySetting(propertyClass, p)).mkString(Properties.lineSeparator)
  }

  def paramValueAndPackage(propertyClass: Class[_], p: Property): (String, Option[String]) = {
    val baseName = packageShortName(propertyClass).getOrElse(propertyClass.getSimpleName)
    p.convertMode match {
      case SYMBOL =>
        (s"\t\t$ENTITY_GET_VALUE[${p.entityType.getSimpleName}]($baseName.${p.entityName}).toSymbol($ENTITY_GET_VALUE[String]($baseName.MARKET))",Some("jp.co.nri.nefs.tool.ViewCodeWrapper._"))
      case BIGDECIMAL =>
        (s"\t\t$ENTITY_GET_VALUE[java.math.BigDecimal]($baseName.${p.entityName})", None)
      case NORMAL =>
        (s"\t\t$ENTITY_GET_VALUE[${p.entityType.getSimpleName}]($baseName.${p.entityName})", None)
    }
  }

  def createParamValues(propertyClass: Class[_], properties: Seq[Property]): String = {
    properties.map(p => paramValueAndPackage(propertyClass, p)._1).mkString("," + Properties.lineSeparator)
  }

  def createParamContent(p: Property): String = {
    s"\t${p.paramName}: ${p.paramTypeName}" + initialValueMapping.get(p.entityName).map(" = " + _).getOrElse(if (p.nullable) " = None" else "")
  }

  def createParamContents(properties: Seq[Property]): String = properties.map(createParamContent)
    .mkString("," + Properties.lineSeparator)

  def createPackageContents(propertyClass: Class[_], properties: List[Property]): String = {

    // ユニークにするためSetに変換
    val uniqueClasses =  (classOf[IEntity[_]] :: classOf[DefaultEntity[_]] :: propertyClass :: properties.map(_.entityType))
      .filter(c => !importExcludePackageList.contains(c.getPackage.getName))
      .filter(c => !importExcludeClassList.contains(c.getName)).toSet

    // 同じパッケージのオブジェクトが3つ以上含まれる場合は、すべてインポートする
    // ただしaliasが登録されているパッケージに属するクラスは取り除く
    val uniqueClasses2 = uniqueClasses.filter{ c =>
      ! packageShortName(c).map(_ => c.getPackage).contains(c.getPackage)
    }
    val aggClasses = for {
      (k, v) <- uniqueClasses2.groupBy(_.getPackage)
      if v.size >= 3
    } yield k

    val packages = uniqueClasses.filter(c => aggClasses.forall(c.getPackage != _))

    val packageNames = packages.map(p => changedPackageName(p).getOrElse(p.getName)).toBuffer

    packageNames ++= aggClasses.map(_.getName + "._")
    packageNames ++= properties.flatMap(p => List(entityValueAndPackage(p)._2, paramValueAndPackage(propertyClass, p)._2))
      .collect{case Some(v) => v}.toSet

    packageNames.map("import " + _).sorted.mkString(Properties.lineSeparator)

  }

  def packageShortName(clazz: Class[_]): Option[String] = packageShortNameMapping.get(clazz.getName)

  def changedPackageName(clazz: Class[_]): Option[String] = {
    val shortName = packageShortName(clazz)
    shortName.map(n => s"${clazz.getPackage.getName}.{${clazz.getSimpleName} => $n}")
  }
}

