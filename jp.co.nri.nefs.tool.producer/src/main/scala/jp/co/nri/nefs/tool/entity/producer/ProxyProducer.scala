package jp.co.nri.nefs.tool.entity.producer

import java.nio.file.{Files, Paths}

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import javax.persistence.Column
import jp.co.nri.nefs.common.di.ComponentContainer
import jp.co.nri.nefs.oms.order.service.proxy.IOrderServiceProxy
import jp.co.nri.nefs.common.util.{property => cp}

import scala.reflect.runtime.{universe => ru}
import ru._
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

object ProxyProducer extends LazyLogging {

  import jp.co.nri.nefs.tool.util.config.RichConfig._

  final val BASE_CONFIG = "ProxyProducer"
  final val OUT_DIR = BASE_CONFIG + ".out-dir"
  final val PARAM_OUT_PACKAGE = BASE_CONFIG + ".param-out-package"
  final val PROXY_OUT_PACKAGE = BASE_CONFIG + ".proxy-out-package"
  final val SCALA_COLLECTION_MAPPING = BASE_CONFIG + ".scala-collection-mapping"
  final val ENTITY_FULL_NAME = "jp.co.nri.nefs.common.model.entity.IEntity"
  final val PROPERTY = "Property"
  final val PARAM = "Param"
  private val config = ConfigFactory.load()
  val scalaCollectionMapping: Map[String, String] = config.getMapping(SCALA_COLLECTION_MAPPING, logger)

  def main(args: Array[String]): Unit = {

    import PropertyProducers._

    val outDir = Paths.get(config.getString(OUT_DIR, logger))
    val paramOutPackage = config.getString(PARAM_OUT_PACKAGE, logger)
    val proxyOutPackage = config.getString(PROXY_OUT_PACKAGE)

    //val mirror = ru.runtimeMirror(getClass. getClassLoader)
    val proxy = ComponentContainer.getComponent(classOf[IOrderServiceProxy])

    val methods = for {
      member <- getType(proxy).tpe.members.sorted
      op = member match {
        case v: MethodSymbol => Some(v)
        case _ => None
      }
      m <- op
      paramList = m.paramLists.headOption
      //先頭の引数の型(IEntity)を取得
      typeSignature = paramList.map(_.map(_.typeSignature).filter(_ != null)).flatMap(_.headOption)
      typeList = typeSignature.map(getTypeList).getOrElse(Nil)
      entityType <- getEntityType(typeList)
      returnList = getTypeList(m.returnType)
      returnEntityType <- getEntityType(returnList)
      collectFunc = getScalaCollection(typeList)
      returnCollectFunc = getScalaCollection(returnList)
      method = Method(m.name.toString, entityType, collectFunc, Class.forName(returnList.head),
        returnEntityType, returnCollectFunc)
      _ = println(s"method=$method")
    } yield method

    for {
      m <- methods.groupBy(_.propertyClass).values.map(_.head)
      properties = getProperties(m.propertyClass)
    } {
      val buffer = ListBuffer[String]()
      buffer += s"package $paramOutPackage"
      buffer += ""
      buffer += createPackageContents(m.propertyClass, properties.toList)
      buffer += ""
      buffer += s"case class ${m.paramClassName}("
      buffer += createParamContents(properties)
      buffer += s") extends AbstractProducer[${m.propertyClassName}] {"
      val shortName = packageShortName(m.propertyClass).getOrElse(m.propertyClassName)
      buffer += s"\tval entity: IEntity[$shortName] = DefaultEntity.valueOf($shortName.class)"
      buffer += createEntitySettings(m.propertyClass, properties)
      buffer += "}"
      buffer += ""
      buffer += s"object ${m.paramClassName} {"
      buffer += s"\tdef apply(entity: IEntity[$shortName]): ${m.paramClassName} = ${m.paramClassName}("
      buffer += s"${createParamValues(m.propertyClass, properties)}"
      buffer += s"\t)"
      buffer += "}"
      val outPath = outDir.resolve(m.paramClassName + ".scala")
      Files.write(outPath, buffer.asJava)
    }
    val buffer = ListBuffer[String]()
    buffer += s"package $proxyOutPackage"
    buffer += ""
    buffer += s"object OrderServiceProxy {"
    buffer += ""
    buffer += s"\tval proxy = ComponentContainer.getComponent(classOf[IOrderServiceProxy])"
    buffer += ""
    for (m <- methods){
      val param = if (m.collectFunc.isDefined) "params" else "param"
      val ret = m.returnCollectFunc.map(f => s"${m.returnClassName}[${f.getSimpleName}[${m.returnParamClassName}]]")
          .getOrElse(s"${m.returnClassName}[${m.returnParamClassName}]")
      val paramClassName = m.collectFunc.map(f => s"${f.getSimpleName}[${m.paramClassName}]").getOrElse(m.paramClassName)
      buffer += s"\tdef ${m.name}($param: $paramClassName): $ret = proxy.${m.name}($param)"
    }

    buffer += ""
    for (m <- methods.groupBy(_.propertyClass).values.map(_.head)) {
      val methodName = Character.toLowerCase(m.paramClassName.charAt(0)) + m.paramClassName.substring(1)
      buffer += s"\timplicit def ${methodName}2Entity(param: ${m.paramClassName}): IEntity[${m.propertyClassName}] = param.entity"
      //  implicit def newOrderEntity2Param(entity: IEntity[ENewOrderProperty]): NewOrderParam = NewOrderParam(entity)
      val methodName2 = methodName.replace(PARAM, "Entity")
      buffer += s"\timplicit def ${methodName2}2Param(entity: IEntity[${m.propertyClassName}]): ${m.paramClassName} = ${m.paramClassName}(entity)"
    }

    buffer += "}"
    val proxyOutPath = outDir.resolve("OrderServiceProxy.scala")
    Files.write(proxyOutPath, buffer.asJava)

    /*val propertyClass = Class.forName("jp.co.nri.nefs.oms.order.service.entity.property.ENewOrderProperty")
    val propertyClassName = propertyClass.getSimpleName
    val paramClassName = propertyClassName.drop(1).replace("Property", "Param")

    val properties = getProperties(propertyClass)

    val buffer = ListBuffer[String]()
    buffer += s"package $paramOutPackage"
    buffer += ""
    buffer += getPackageContents(propertyClass, properties.map(_.entityType).toList)
    buffer += ""
    buffer += s"case class $paramClassName("
    buffer += getParamContents(properties)
    buffer += s") extends AbstractProducer[$propertyClassName] {"
    buffer += s"\tval entity = DefaultEntity.valueOf($propertyClassName.class)"
    buffer += getEntitySettings(propertyClassName, properties)
    buffer += "}"
    buffer += ""
    buffer += s"object $paramClassName {"
    buffer += s"\tdef apply(entity: IEntity[$propertyClassName]): $paramClassName ="
    buffer += s"${getParamSettings(paramClassName, propertyClassName, properties)}"
    buffer += "}"

    val outPath = outDir.resolve (paramClassName + ".scala")
    Files.write(outPath, buffer.asJava)*/
  }

  def getType[T: ru. TypeTag](obj: T): ru.TypeTag[T] = ru.typeTag

  def getTypeList(t: ru.Type): List[String] = t.typeSymbol.fullName ::
    t.typeArgs.headOption.map(getTypeList).getOrElse(Nil)

  def getEntityType(classes: List[String]): Option[Class[_]] = {
    val name = classes.zipWithIndex.collectFirst{case (clazz, index) if clazz == ENTITY_FULL_NAME =>
      index
    }.flatMap { i =>
      if (i >= classes.size -1) None else Some(classes(i + 1))
    }
    name.map(Class.forName)
  }

  def getScalaCollection(javaClassName: String): Option[Class[_]] = {
    scalaCollectionMapping.get(javaClassName).map(Class.forName)
  }

  def getScalaCollection(classes: List[String]): Option[Class[_]] = {
    classes.map(s => getScalaCollection(s)).collectFirst{
      case Some(clazz) => clazz
    }
  }

  def getProperties(entityType: Class[_]): Seq[Property] = {
    for {
      e <- entityType.getEnumConstants.map(_.asInstanceOf[Enum[_]])
      field = e.getDeclaringClass.getDeclaredField(e.name())
      nullable = field.getAnnotation(classOf[Column]).nullable()
      entityType = field.getAnnotation(classOf[cp.Type]).value()
      property = Property(e.name(), entityType, nullable)
    } yield  property
  }

}

case class Method(name: String, propertyClass: Class[_], collectFunc: Option[Class[_]],
                  returnClass: Class[_], returnPropertyClass: Class[_], returnCollectFunc: Option[Class[_]]) {
  val propertyClassName: String = propertyClass.getSimpleName
  val paramClassName: String = propertyClassName.drop(1).replace(ProxyProducer.PROPERTY, ProxyProducer.PARAM)
  val returnClassName: String = returnClass.getSimpleName
  val returnPropertyClassName: String = returnPropertyClass.getSimpleName
  val returnParamClassName: String = returnPropertyClassName.drop(1).replace(ProxyProducer.PROPERTY, ProxyProducer.PARAM)
}
