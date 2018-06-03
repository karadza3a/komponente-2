package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LessonRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import profile.api._

  private class LessonTable(tag: Tag) extends Table[Lesson](tag, "lessons") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def subject = column[String]("subject")

    def category = column[String]("category")

    def professor = column[String]("professor")

    def groups = column[String]("groups")

    def dayOfWeek = column[String]("dayOfWeek")

    def timeStart = column[String]("timeStart")

    def timeEnd = column[String]("timeEnd")

    def room = column[String]("room")

    def * = (id, subject, category, professor, groups, dayOfWeek, timeStart, timeEnd, room) <>
      ((Lesson.applyFromStrings _).tupled, Lesson.unapplyToStrings)
  }

  private val lessons = TableQuery[LessonTable]

  def list(): Future[Seq[Lesson]] = db.run {
    lessons.result
  }
}
