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

  def getEvents: Action[AnyContent] = Action.async { implicit request =>
    val jsonString = "[{\"title\":\"Integrisani informacioni sistemi [P]\",\"description\":\"Predavač: Vujosevic Dusan, grupe: 401,402\",\"location\":\"U6\",\"start\":\"2018-06-01T12:15:00.000+02:00\",\"end\":\"2018-06-01T14:00:00.000+02:00\"},{\"title\":\"Integrisani informacioni sistemi [V]\",\"description\":\"Predavač: Mijailovic Tina, grupe: 401,402\",\"location\":\"U5\",\"start\":\"2018-06-01T14:15:00.000+02:00\",\"end\":\"2018-06-01T16:00:00.000+02:00\"},{\"title\":\"Konkurentni i distribuirani sistemi [P]\",\"description\":\"Predavač: Milinkovic Stevan, grupe: 401,402,403\",\"location\":\"U3\",\"start\":\"2018-06-01T09:15:00.000+02:00\",\"end\":\"2018-06-01T12:00:00.000+02:00\"},{\"title\":\"Softversko inzenjerstvo [V]\",\"description\":\"Predavač: MarkovicAna, grupe: 401\",\"location\":\"U4\",\"start\":\"2018-06-05T09:15:00.000+02:00\",\"end\":\"2018-06-05T13:00:00.000+02:00\"},{\"title\":\"Softversko inzenjerstvo [P]\",\"description\":\"Predavač: Perisic Branko, grupe: 401,402\",\"location\":\"U6\",\"start\":\"2018-06-05T15:15:00.000+02:00\",\"end\":\"2018-06-05T19:00:00.000+02:00\"},{\"title\":\"Teorija algoritama, automata i jezika [V]\",\"description\":\"Predavač: Tomic Milan, grupe: 401\",\"location\":\"U3\",\"start\":\"2018-06-06T18:15:00.000+02:00\",\"end\":\"2018-06-06T21:00:00.000+02:00\"},{\"title\":\"Teorija algoritama, automata i jezika [P]\",\"description\":\"Predavač: Jovanovic Jelena, grupe: 401,402\",\"location\":\"U1\",\"start\":\"2018-06-06T15:15:00.000+02:00\",\"end\":\"2018-06-06T18:00:00.000+02:00\"},{\"title\":\"Konkurentni i distribuirani sistemi [V]\",\"description\":\"Predavač: Milojkovic Branislav, grupe: 401\",\"location\":\"U7\",\"start\":\"2018-06-07T09:15:00.000+02:00\",\"end\":\"2018-06-07T12:00:00.000+02:00\"},{\"title\":\"Integrisani informacioni sistemi [P]\",\"description\":\"Predavač: Vujosevic Dusan, grupe: 401,402\",\"location\":\"U6\",\"start\":\"2018-06-08T12:15:00.000+02:00\",\"end\":\"2018-06-08T14:00:00.000+02:00\"},{\"title\":\"Integrisani informacioni sistemi [V]\",\"description\":\"Predavač: Mijailovic Tina, grupe: 401,402\",\"location\":\"U5\",\"start\":\"2018-06-08T14:15:00.000+02:00\",\"end\":\"2018-06-08T16:00:00.000+02:00\"},{\"title\":\"Konkurentni i distribuirani sistemi [P]\",\"description\":\"Predavač: Milinkovic Stevan, grupe: 401,402,403\",\"location\":\"U3\",\"start\":\"2018-06-08T09:15:00.000+02:00\",\"end\":\"2018-06-08T12:00:00.000+02:00\"},{\"title\":\"Softversko inzenjerstvo [V]\",\"description\":\"Predavač: MarkovicAna, grupe: 401\",\"location\":\"U4\",\"start\":\"2018-06-12T09:15:00.000+02:00\",\"end\":\"2018-06-12T13:00:00.000+02:00\"},{\"title\":\"Softversko inzenjerstvo [P]\",\"description\":\"Predavač: Perisic Branko, grupe: 401,402\",\"location\":\"U6\",\"start\":\"2018-06-12T15:15:00.000+02:00\",\"end\":\"2018-06-12T19:00:00.000+02:00\"},{\"title\":\"Teorija algoritama, automata i jezika [V]\",\"description\":\"Predavač: Tomic Milan, grupe: 401\",\"location\":\"U3\",\"start\":\"2018-06-13T18:15:00.000+02:00\",\"end\":\"2018-06-13T21:00:00.000+02:00\"},{\"title\":\"Teorija algoritama, automata i jezika [P]\",\"description\":\"Predavač: Jovanovic Jelena, grupe: 401,402\",\"location\":\"U1\",\"start\":\"2018-06-13T15:15:00.000+02:00\",\"end\":\"2018-06-13T18:00:00.000+02:00\"},{\"title\":\"Konkurentni i distribuirani sistemi [V]\",\"description\":\"Predavač: Milojkovic Branislav, grupe: 401\",\"location\":\"U7\",\"start\":\"2018-06-14T09:15:00.000+02:00\",\"end\":\"2018-06-14T12:00:00.000+02:00\"}]"

    Future.successful(jsonString).map { _ =>
      Ok(Json.toJson(Json.parse(jsonString)))
    }
  }
}
