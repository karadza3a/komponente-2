package controllers

import javax.inject._
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class StudentController @Inject()(userAction: UserInfoAction,
                                  ws: WSClient,
                                  cc: ControllerComponents) extends AbstractController(cc) {

  def sync = userAction { implicit request: UserRequest[_] =>
    Ok(views.html.sync(form))
  }

  def lessons = userAction { implicit request: UserRequest[_] =>
    Ok(views.html.lessons(form))
  }

  case class Lesson(id: Long,
                    subject: String,
                    category: String,
                    professor: String,
                    groups: String,
                    dayOfWeek: String,
                    timeStart: String,
                    timeEnd: String,
                    room: String)

  def getLessons(group: String, room: String, dayOfWeek: String): Action[AnyContent] = Action.async { implicit request =>
    ws.url("http://localhost:9091/lessons")
      .addQueryStringParameters(("group", group), ("room", room), ("dayOfWeek", dayOfWeek))
      .get().map { response =>
      response.status match {
        case 200 =>
          implicit val lessonReads: Reads[Lesson] = Json.reads[Lesson]
          val lessons: List[Lesson] = response.json.as[List[Lesson]]
          val lessonList = lessons.map(l =>
            (l.subject, l.category, l.professor, l.groups, l.room, l.dayOfWeek, l.timeStart, l.timeEnd))
          Ok(Json.toJson(lessonList))

        case _ => InternalServerError(response.statusText)
      }
    }
  }
}
