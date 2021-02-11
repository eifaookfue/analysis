package jp.co.nri.tcatool.common.read

import java.sql.Timestamp
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime, LocalTime}

import com.typesafe.scalalogging.LazyLogging

import scala.util.{Success, Try}

trait Read[A] {
  final val NULL = "NULL"
  def reads(s: String): Try[A]
  def optionalReads(s: String): Try[Option[A]] = {
    Try(Option(s).map(_.trim).filter(_.nonEmpty).filter(_ != NULL).flatMap(reads(_).toOption))
  }
}

object Read extends LazyLogging {

  private val pattern = "yyyy-MM-dd HH:mm:ss"
  private val formatter = java.time.format.DateTimeFormatter.ofPattern(pattern)

  def apply[A](implicit readA: Read[A]): Read[A] = readA

  implicit object stringRead extends Read[String] {
    def reads(s: String): Try[String] = Success(s.trim)
  }

  implicit object intRead extends Read[Int] {
    def reads(s: String): Try[Int] = {
      Try(s.trim.toInt)
    }
  }

  implicit object localTimeRead extends Read[LocalTime] {
    private val timePattern = "[ ]Hmmss"
    private val timeFormatter = DateTimeFormatter.ofPattern(timePattern)
    def reads(s: String): Try[LocalTime] = Try(LocalTime.parse(s.trim, timeFormatter))
  }

  object timeMinutesRead extends Read[LocalTime] {
    private val timePattern = "[ ]Hmm"
    private val timeFormatter = DateTimeFormatter.ofPattern(timePattern)
    def reads(s: String): Try[LocalTime] = Try(LocalTime.parse(s.trim, timeFormatter))
  }

  implicit object timeStampRead extends Read[Timestamp] {
    private def timestampParse(data: String) = Timestamp.valueOf(LocalDateTime.parse(data, formatter))
    def reads(s: String) = Try(timestampParse(s.trim))
  }

  implicit object dateRead extends Read[LocalDate] {
    def reads(s: String): Try[LocalDate] = Try(LocalDate.parse(s.trim, formatter))
  }

  implicit object bigDecimalRead extends Read[BigDecimal] {
    def reads(s: String): Try[BigDecimal] = {
      Try(BigDecimal(s.trim).underlying().stripTrailingZeros())
    }
  }

}
