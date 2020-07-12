package jp.co.nri.nefs.tool.util.data

import jp.co.nri.nefs.tool.util.data.format.Formatter
import org.apache.poi.ss.usermodel.Row

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
  def withPrefix(prefix: Int): Mapping[T]
}



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
}

case class FieldMapping[T](index: Int = 0)(implicit val binder: Formatter[T]) extends Mapping[T] {

  override def bind(row: Row): Either[Seq[LineError], T] = {
    binder.bind(index, row)
  }

  override def unbind(value: T, row: Row): Unit = {
    binder.unbind(index, value, row)
  }

  override def withPrefix(prefix: Int): Mapping[T] = this.copy(index = prefix)

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