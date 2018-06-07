package controllers

import javax.inject._
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class StudentController @Inject()(userAction: UserInfoAction,
                                  ws: WSClient,
                                  cc: ControllerComponents) extends AbstractController(cc) {

  val authorizedUserAction: (UserRequest[_] => Future[Result]) => Action[AnyContent] =
    userAction.authorizedUserAction(userInfo => userInfo.isDefined && !userInfo.get.isAdmin)

  def sync: Action[AnyContent] = authorizedUserAction { implicit request: UserRequest[_] =>
    Future.successful(Ok(views.html.sync(request)))
  }

  def lessons: Action[AnyContent] = authorizedUserAction { implicit request: UserRequest[_] =>
    Future.successful(Ok(views.html.lessons(request)))
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

  def getLessons(group: String, room: String, dayOfWeek: String): Action[AnyContent] = authorizedUserAction {
    implicit request =>
      ws.url("http://localhost:9091/lessons")
        .addQueryStringParameters("group" -> group, "room" -> room, "dayOfWeek" -> dayOfWeek)
        .get().map {
        response =>
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

  def getEvents(start: String, end: String): Action[AnyContent] = authorizedUserAction { implicit request =>
    val id = request.userInfo.get.id.toString
    ws.url("http://localhost:9092/sync")
      .addQueryStringParameters("id" -> id, "start" -> start, "end" -> end).get().map {
      response =>
        response.status match {
          case 200 => Ok(response.body)
          case _ => InternalServerError(response.statusText)
        }
    }
  }
}
