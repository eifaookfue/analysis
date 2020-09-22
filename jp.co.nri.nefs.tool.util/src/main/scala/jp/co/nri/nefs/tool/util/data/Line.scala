package jp.co.nri.nefs.tool.util.data

import jp.co.nri.nefs.tool.util.data.format.Formatter
import org.apache.poi.ss.usermodel.{Cell, Row}

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

  /**
    * Transform this Mapping[T] to a Mapping[B].
    *
    * @tparam B The type of the new mapping.
    * @param f1 Transform value of T to a value of B
    * @param f2 Transform value of B to a value of T
    */
  def transform[B](f1: T => B, f2: B => T): Mapping[B] = WrappedMapping(this, f1, f2)

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

/**
  * A mapping wrapping another existing mapping with transformation functions.
  *
  * @param wrapped Existing wrapped mapping
  * @param f1 Transformation function from A to B
  * @param f2 Transformation function from B to A
  */
case class WrappedMapping[A, B](wrapped: Mapping[A], f1: A => B, f2: B => A) extends Mapping[B] {

  /**
    * The field key.
    */
  override val key: Key = wrapped.key

  /**
    * Binds this field, i.e. construct a concrete value from submitted data.
    *
    * @param row the submitted data
    * @return either a concrete value of type `B` or a set of errors, if the binding failed
    */
  override def bind(row: Row): Either[Seq[LineError], B] = {
    wrapped.bind(row).right.map(t => f1(t))
  }

  /**
    * Unbinds this field, i.e. transforms a concrete value to plain data.
    *
    * @param value the value to unbind
    * @return the plain data
    */
  override def unbind(value: B, row: Row): Unit = wrapped.unbind(f2(value), row)

  def withKey(key: Key): Mapping[B] = {
    this.copy(wrapped = wrapped.withKey(key))
  }

  override def withParamName(paramName: String): Mapping[B] = {
    this.copy(wrapped = wrapped.withParamName(paramName))
  }

  override val paramName: String = wrapped.paramName

  override def paramNames: Seq[String] = wrapped.paramNames

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

  /**
    * Changes the binder used to handle this field.
    *
    * @param binder the new binder to use
    * @return the same mapping with a new binder
    */
  def as(binder: Formatter[T]): Mapping[T] = {
    this.copy()(binder)
  }


}

case class OptionalMapping[T](wrapped: Mapping[T]) extends Mapping[Option[T]] {

  override val key: Key = wrapped.key

  override def bind(row: Row): Either[Seq[LineError], Option[T]] = {
    Option(row.getCell(key.index)).flatMap{cell =>
      if (cell.getCellType == Cell.CELL_TYPE_BLANK)
        None
      else
        Option(cell)
    }.map{_ => wrapped.bind(row).right.map(Some(_))}.getOrElse(Right(None))
  }

  override def unbind(value: Option[T], row: Row): Unit = {
    value.foreach(wrapped.unbind(_, row))
  }

  override def withKey(key: Key): Mapping[Option[T]] = {
    this.copy(wrapped = wrapped.withKey(key))
  }

  override def withParamName(paramName: String): Mapping[Option[T]] = {
    this.copy(wrapped = wrapped.withParamName(paramName))
  }

  override val paramName: String = wrapped.paramName

  override def paramNames: Seq[String] = wrapped.paramNames

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