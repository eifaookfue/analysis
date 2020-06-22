package jp.co.nri.nefs.tool.analytics.training

import java.text.SimpleDateFormat
import java.util.{Calendar, Date, Locale}

object JapaneseFormat {

  def main(args: Array[String]): Unit = {
    val locale = new Locale("ja", "JP", "JP")
    val calendar = Calendar.getInstance(locale)

    val japaneseFormat = new SimpleDateFormat("GGGGy年", locale)
    val s = japaneseFormat.format(new Date)
    println(s)
    val adFormat = new SimpleDateFormat("yyyy")
    val a = adFormat.parse("1990")
    val b = adFormat.parse("1989")
    val c = adFormat.parse("1988")
    val d = adFormat.parse("1987")
    println(japaneseFormat.format(a))
    println(japaneseFormat.format(b))
    println(japaneseFormat.format(c))
    println(japaneseFormat.format(d))

  }
}
