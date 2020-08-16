package jp.co.nri.nefs.tool.util.data

import jp.co.nri.nefs.tool.util.data.format.Formatter
import org.apache.poi.ss.usermodel.Row

import scala.reflect.runtime.{universe => ru}
import ru._

case class Line[T](mapping: Mapping[T], row: Row, errors: Seq[LineError], value: Option[T]) {

  def bind(row: Row): Line[T] = mapping.bind(row).fold(
    newErrors => this.copy(row = row, errors = errors ++ newErrors, value = None),
    value => this.copy(row = row, errors = errors, value = Some(value)))

  def fold[R](hasErrors: Line[T] => R, success: T => R): R = value match {
    case Some(v) if errors.isEmpty => success(v)
    case _ => hasErrors(this)
  }

  def get: T = value.get


}

object Line {
  def apply[T](mapping: Mapping[T]): Line[T] = Line(mapping, null, Nil, None)
}

trait Mapping[T] {
  def bind(row: Row): Either[Seq[LineError], T]
  def unbind(value: T, row: Row): Unit
  def withKey(key: Key): Mapping[T]
  def withParamName(paramName: String): Mapping[T]
  val key: Key
  val paramName: String

  protected def addKey(newKey: Key): Option[Key] = {
    Option(key).map { key1 =>
      Key(key1.index + Option(newKey).map(_.index).getOrElse(0), Option(newKey).map(_.count).getOrElse(key1.count))
    }.orElse(Some(newKey))
  }

  def repeatingCount: Int = 1

  /*
    Get the list of parameter names which this class have including child parameter names.
   */
  def paramNames: Seq[String]

}

protected case class Key(index: Int, count: Int)

trait ObjectMapping {

  def merge2(a: Either[Seq[LineError], Seq[Any]], b: Either[Seq[LineError], Seq[Any]]): Either[Seq[LineError], Seq[Any]] = (a, b) match {
    case (Left(errorsA), Left(errorsB)) => Left(errorsA ++ errorsB)
    case (Left(errorsA), Right(_)) => Left(errorsA)
    case (Right(_), Left(errorsB)) => Left(errorsB)
    case (Right(x), Right(y)) => Right(x ++ y)
  }

  def merge(results: Either[Seq[LineError], Any]*): Either[Seq[LineError], Seq[Any]] = {
    val all: Seq[Either[Seq[LineError], Seq[Any]]] = results.map(_.right.map(Seq(_)))
    all.fold(Right(Nil)){ (s, i) => merge2(s, i)}
  }

  def paramNames(tpe: Type): Seq[String] = {
    val constructor = tpe.decl(termNames.CONSTRUCTOR).asMethod
    constructor.paramLists.head.map(_.name.toString)
  }

  /*def paramNames[T](mappings: Mapping[_]*)(implicit evidence: TypeTag[T]): Option[Seq[String]] = {
    val constructor = evidence.tpe.decl(termNames.CONSTRUCTOR).asMethod
    val paramList = constructor.paramLists.head.map(_.name.toString)
    val l = for {
      ((childCount, childNames), parentName) <- mappings.map(m => (m.repeatingCount, m.paramNames)) zip paramList
    } yield {
      childNames.map{cNames =>
        for {
          i <- 0 until childCount
          cName <- cNames
        } yield s"$parentName[$i].$cName"
      }.getOrElse(Seq(parentName))
    }
    Option(l.flatten).filter(_.nonEmpty)
  }*/
}

case class FieldMapping[T](key: Key = null, paramName: String = null)(implicit val binder: Formatter[T]) extends Mapping[T] {

  override def bind(row: Row): Either[Seq[LineError], T] = {
    binder.bind(key.index, row)
  }

  override def unbind(value: T, row: Row): Unit = {
    binder.unbind(key.index, value, row)
  }

  override def withKey(key: Key): Mapping[T] = addKey(key).map{ newKey =>
    this.copy(key = newKey)
  }.getOrElse(this)

  override def withParamName(paramName: String): Mapping[T] = this.copy(paramName = paramName)

  override def paramNames: Seq[String] = Seq(paramName)

}

case class RepeatedMapping[T](
                               wrapped: Mapping[T],
                               key: Key = null,
                               paramName: String = null
                             )(implicit evidence: TypeTag[T]) extends Mapping[List[T]] {


  lazy val constructor: MethodSymbol = evidence.tpe.decl(termNames.CONSTRUCTOR).asMethod
  // Get arguments of of the first constructor
  lazy val paramList: List[Symbol] = constructor.paramLists.head
  lazy val paramSize: Int = wrapped.paramNames.size

  override def bind(row: Row): Either[Seq[LineError], List[T]] = {
    val start = key.index
    val step = paramSize
    val end = start + step * (repeatingCount - 1)
    val allErrorsOrItems: Seq[Either[Seq[LineError], T]] =
      (start to end by step).map{i =>
        wrapped.withKey(Key(i, repeatingCount)).bind(row)
      }

    if (allErrorsOrItems.forall(_.isLeft))
      Left(allErrorsOrItems.flatMap(_.left.get))
    else
      Right(allErrorsOrItems.collect { case Right(right) => right }.toList)
  }

  override def unbind(value: List[T], row: Row): Unit = {
    val start = key.index
    val step = paramSize
    value.zipWithIndex.foreach{ case (v, i) =>
      wrapped.withKey(Key(start + i * step, repeatingCount)).unbind(v, row)
    }

    //wrapped.withIndex(key).unbind(value, row)
  }

  override def withKey(key: Key): Mapping[List[T]] = addKey(key).map { newKey =>
    this.copy(key = newKey)
  }.getOrElse(this)

  override def withParamName(paramName: String): Mapping[List[T]] = this.copy(paramName = paramName)

  override def paramNames: Seq[String] = {
    for {
      i <- 0 until repeatingCount
      childParam <- wrapped.paramNames
    } yield s"$paramName[$i].$childParam"
  }

  override def repeatingCount: Int = key.count

}

case class LineError(index: Int, messages: Seq[String], args: Seq[Any] = Nil) {

  def this(index: Int, message: String) = this(index, Seq(message), Nil)

  def this(index: Int, message: String, args: Seq[Any]) = this(index, Seq(message), args)

  lazy val message: String = messages.last
}

object LineError {

  def apply(index: Int, message: String) = new LineError(index, message)

  def apply(index: Int, message: String, args: Seq[Any]) = new LineError(index, message, args)
}