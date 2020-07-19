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
  def withIndex(index: String): Mapping[T]
  val key: String

  protected def addIndex(index: String): Option[String] = {
    Option(index).filterNot(_.isEmpty).map(p => p.toInt + Option(key).filterNot(_.isEmpty).map(_.toInt).getOrElse(0)).map(_.toString)
  }


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

case class FieldMapping[T](key: String = "")(implicit val binder: Formatter[T]) extends Mapping[T] {

  override def bind(row: Row): Either[Seq[LineError], T] = {
    binder.bind(key.toInt, row)
  }

  override def unbind(value: T, row: Row): Unit = {
    binder.unbind(key.toInt, value, row)
  }

  //override def withIndex(index: String): Mapping[T] = addIndex(index).map(newKey => this.copy(key = newKey)).getOrElse(this)
  override def withIndex(index: String): Mapping[T] = addIndex(index).map{ newKey =>
    this.copy(key = newKey)
  }.getOrElse(this)

}

case class RepeatedMapping[T](
                               wrapped: Mapping[T],
                               key: String = "",
                             ) extends Mapping[List[T]] {

  lazy val (start, end, step): (Int, Int, Int) = range(key)

  private def range(str: String): (Int, Int, Int) = {
    require(str.contains("x") && str.contains("@"), s"$str must be contained x and @")
    str.split("@") match {
      case Array(mat, pos) =>
        mat.split("x") match {
          case Array(row, col) =>  // 2x1@4  row=2 col=1 step=2
            val start = pos.toInt
            val step = row.toInt
            val end = start + (col.toInt - 1) * step
            (start, end, step)
        }
    }
  }

  override def bind(row: Row): Either[Seq[LineError], List[T]] = {
    val allErrorsOrItems: Seq[Either[Seq[LineError], T]] =
      //(0 to size).map(i => wrapped.withIndex((position + i).toString).bind(row))
      (start to end by step).map{i =>
        wrapped.withIndex(i.toString).bind(row)
      }

    if (allErrorsOrItems.forall(_.isLeft))
      Left(allErrorsOrItems.flatMap(_.left.get))
    else
      Right(allErrorsOrItems.collect { case Right(right) => right }.toList)
  }

  override def unbind(value: List[T], row: Row): Unit = {
    //value.foreach(v => wrapped.withIndex(key).unbind(v, row))
    value.zipWithIndex.foreach{ case (v, i) =>
      wrapped.withIndex((start + i * step).toString).unbind(v, row)
    }

    //wrapped.withIndex(key).unbind(value, row)
  }

  override def withIndex(index: String): Mapping[List[T]] = this.copy(key = index)

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