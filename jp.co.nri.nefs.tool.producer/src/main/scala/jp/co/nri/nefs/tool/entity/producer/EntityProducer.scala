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

object EntityProducer extends LazyLogging {

  import jp.co.nri.nefs.tool.util.config.RichConfig._

  final val BASE_CONFIG = "EntityProducer"
  final val OUT_DIR = BASE_CONFIG + ".out-dir"
  final val OUT_PACKAGE = BASE_CONFIG + ".out-package"
  final val SCALA_COLLECTION_MAPPING = BASE_CONFIG + ".scala-collection-mapping"
  final val ENTITY_FULL_NAME = "jp.co.nri.nefs.common.model.entity.IEntity"
  private val config = ConfigFactory.load()
  val scalaCollectionMapping: Map[String, String] = config.getMapping(SCALA_COLLECTION_MAPPING, logger)

  def main(args: Array[String]): Unit = {

    import PropertyProducers._

    val outDir = Paths.get(config.getString(OUT_DIR, logger))
    val outPackage = config.getString(OUT_PACKAGE, logger)


    //val mirror = ru.runtimeMirror(getClass. getClassLoader)
    val proxy = ComponentContainer.getComponent(classOf[IOrderServiceProxy])

    val methods = getType(proxy).tpe.members.sorted.collect {
      case m: MethodSymbol =>
        val paramList = m.paramLists.headOption
        //先頭の引数の型(IEntity)を取得
        val typeSignature = paramList.map(_.map(_.typeSignature).filter(_ != null)).flatMap(_.headOption)
        // IEntityの先頭の型引数を取得
        //val typeName = typeSignature. flatMap C. typeArgs. headOption). map (.toString)
        //println(typeSignature.map(t => Class.forName(t.toString)))
        println((m.name.toString, typeSignature))
        val typeList = typeSignature.map(getTypeList).getOrElse(Nil)
        val entityType = getEntityType(typeList)
        (entityType.isDefined, Method(m.name.toString, entityType.orNull, getScalaCollection(typeList)))
    }.collect {
      case (true, m: Method) => m
    }
    for {
      method <- methods
      propertyClass = method.entityType
    } println("")

    val propertyClass = Class.forName("jp.co.nri.nefs.oms.order.service.entity.property.ENewOrderProperty")
    val propertyClassName = propertyClass.getSimpleName
    val paramClassName = propertyClassName.drop(1).replace("Property", "Param")

    val properties = for {
      e <- propertyClass.getEnumConstants.map(_.asInstanceOf[Enum[_]])
      field = e.getDeclaringClass.getDeclaredField(e.name())
      nullable = field.getAnnotation(classOf[Column]).nullable()
      entityType = field.getAnnotation(classOf[cp.Type]).value()
      property = Property(e.name(), entityType, nullable)
    } yield  property

    val buffer = ListBuffer[String]()
    buffer += s"package $outPackage"
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
    Files.write(outPath, buffer.asJava)
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

}

case class Method(name: String, entityType: Class[_], collectFunc: Option[Class[_]])
