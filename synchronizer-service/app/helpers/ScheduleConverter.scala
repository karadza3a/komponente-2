package helpers

import java.time.DayOfWeek

import org.joda.time.{Days, LocalDate}

class ScheduleConverter(start: LocalDate,
                        end: LocalDate,
                        lessons: List[Lesson]) {

  private val lessonsMap: Map[DayOfWeek, List[Lesson]] = lessons.groupBy(_.dayOfWeek)

  def calendarEvents: List[CalendarEvent] = {
    val daysCount = Days.daysBetween(start, end).getDays
    val dates = (0 until daysCount).map(start.plusDays).toList

    dates.flatMap { date =>
      val lessonsForDay: List[Lesson] = lessonsMap.getOrElse(DayOfWeek.of(date.getDayOfWeek), List())

      lessonsForDay.map { lesson =>
        CalendarEvent(lesson, date)
      }
    }
  }
}
