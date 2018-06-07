package helpers

import org.joda.time.{DateTime, DateTimeZone, LocalDate}
import play.api.libs.json.{Json, Writes}

case class CalendarEvent(title: String,
                         description: String,
                         location: String,
                         start: DateTime,
                         end: DateTime) {}

object CalendarEvent {
  def apply(lesson: Lesson, date: LocalDate, timeZone: DateTimeZone = DateTimeZone.forID("Europe/Belgrade")): CalendarEvent = {
    val cat: String = lesson.category match {
      case "Predavanja" => "P"
      case "Vezbe" => "V"
      case "Predavanja i vezbe" => "P+V"
      case "Laboratorijske vezbe" => "Lab"
      case _ => lesson.category
    }
    new CalendarEvent(
      s"${lesson.subject} [$cat]",
      s"Predavač: ${lesson.professor}, grupe: ${lesson.groups}",
      lesson.room,
      date.toDateTime(lesson.timeStart).withZone(timeZone),
      date.toDateTime(lesson.timeEnd).withZone(timeZone)
    )
  }

  implicit val implicitLessonWrites: Writes[CalendarEvent] = (e: CalendarEvent) => {
    Json.obj(
      "title" -> e.title,
      "description" -> e.description,
      "location" -> e.location,
      "start" -> e.start.toString,
      "end" -> e.end.toString,
    )
  }
}
