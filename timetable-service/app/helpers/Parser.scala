package helpers

import java.io.{FileInputStream, InputStreamReader}
import java.time.DayOfWeek

import com.univocity.parsers.csv.{CsvParser, CsvParserSettings}
import models.Lesson
import org.joda.time.LocalTime

import scala.collection.JavaConverters._

class Parser(var path: String, parserSettings: CsvParserSettings = new CsvParserSettings()) {

  private val parser = new CsvParser(parserSettings)

  private def parseDayOfWeek(str: String): DayOfWeek = {
    str match {
      case "PON" => DayOfWeek.MONDAY
      case "UTO" => DayOfWeek.TUESDAY
      case "SRE" => DayOfWeek.WEDNESDAY
      case "ČET" => DayOfWeek.THURSDAY
      case "PET" => DayOfWeek.FRIDAY
      case "SUB" => DayOfWeek.SATURDAY
      case "NED" => DayOfWeek.SUNDAY
    }
  }

  /**
    * Replaces unbreakable spaces (U+0160) with regular spaces and trims the resulting string string
    *
    * @param str the string to trim
    * @return trimmed string
    */
  private def trimmed(str: String): String = str.replace('\u00A0', ' ').trim

  def parseLessons: List[Lesson] = {
    val csv: List[Array[String]] = parser.parseAll(
      new InputStreamReader(new FileInputStream(path), "UTF-8")
    ).asScala.toList

    csv.slice(1, csv.size).map { row =>
      Lesson(
        0,
        row(0),
        row(1),
        row(2),
        row(3).split(",").map(trimmed).mkString(","),
        parseDayOfWeek(trimmed(row(4))),
        LocalTime.parse(row(5).split("-")(0)),
        LocalTime.parse(row(5).split("-")(1)),
        trimmed(row(6))
      )
    }
  }

}
