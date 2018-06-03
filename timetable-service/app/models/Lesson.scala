package models

import java.time.DayOfWeek

import org.joda.time.LocalTime
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Lesson(id: Long,
                  subject: String,
                  category: String,
                  professor: String,
                  groups: String,
                  dayOfWeek: DayOfWeek,
                  timeStart: LocalTime,
                  timeEnd: LocalTime,
                  room: String)

object Lesson {
  implicit val implicitLessonWrites: Writes[Lesson] = (l: Lesson) => {
    Json.obj(
      "id" -> l.id,
      "subject" -> l.subject,
      "category" -> l.category,
      "professor" -> l.professor,
      "groups" -> l.groups,
      "dayOfWeek" -> l.dayOfWeek.toString,
      "timeStart" -> l.timeStart.toString,
      "timeEnd" -> l.timeEnd.toString,
      "room" -> l.room
    )
  }

  implicit val implicitLessonReads: Reads[Lesson] = (
    (JsPath \ "id").read[Long] and
      (JsPath \ "subject").read[String] and
      (JsPath \ "category").read[String] and
      (JsPath \ "professor").read[String] and
      (JsPath \ "groups").read[String] and
      (JsPath \ "dayOfWeek").read[String] and
      (JsPath \ "timeStart").read[String] and
      (JsPath \ "timeEnd").read[String] and
      (JsPath \ "room").read[String]
    ) (Lesson.applyFromStrings _)

  implicit val lessonFormat: Format[Lesson] = Format(implicitLessonReads, implicitLessonWrites)

  def applyFromStrings(id: Long, subject: String, category: String, professor: String, groups: String, dayOfWeek: String,
                       timeStart: String, timeEnd: String, room: String): Lesson = {
    new Lesson(id, subject, category, professor, groups, DayOfWeek.valueOf(dayOfWeek), LocalTime.parse(timeStart),
      LocalTime.parse(timeEnd), room)
  }

  def unapplyToStrings(l: Lesson): Option[(Long, String, String, String, String, String, String, String, String)] = {
    Some((l.id, l.subject, l.category, l.professor, l.groups, l.dayOfWeek.toString, l.timeStart.toString,
      l.timeEnd.toString, l.room))
  }
}
