package controllers

import javax.inject._
import models._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class LessonController @Inject()(repo: LessonRepository,
                               cc: MessagesControllerComponents
                              )(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  def getLessons: Action[AnyContent] = Action.async { implicit request =>
    repo.list().map { lessons =>
      Ok(Json.toJson(lessons))
    }
  }
}
