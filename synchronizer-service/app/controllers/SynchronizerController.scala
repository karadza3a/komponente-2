package controllers

import helpers.{Lesson, ScheduleConverter}
import javax.inject._
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SynchronizerController @Inject()()(ws: WSClient,
                                         cc: ControllerComponents) extends AbstractController(cc) {

  def synchronize(userId: Long, startString: String, endString: String): Action[AnyContent] = Action.async { implicit request =>
    val eventualGroup: Future[String] = ws.url(s"http://localhost:9090/user/$userId").get().map { response =>
      response.status match {
        case 200 => (response.json \ "group").as[String]
        case _ => throw new RuntimeException(s"user API call returned ${response.statusText}")
      }
    }

    val eventualLessons: Future[List[Lesson]] = eventualGroup.flatMap { group =>
      ws.url("http://localhost:9091/lessons")
        .addQueryStringParameters(("group", group))
        .get().map { response =>
        response.status match {
          case 200 => response.json.as[List[Lesson]]
          case _ => throw new RuntimeException(s"lessons API call returned ${response.statusText}")
        }
      }
    }

    eventualLessons.map { lessons =>
      val start = DateTime.parse(startString).toLocalDate
      val end = DateTime.parse(endString).toLocalDate

      Ok(Json.toJson(new ScheduleConverter(start, end, lessons).calendarEvents))
    }
  }
}
