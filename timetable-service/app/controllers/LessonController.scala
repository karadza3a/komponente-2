package controllers

import helpers.Parser
import javax.inject._
import models._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

class LessonController @Inject()(repo: LessonRepository,
                                 cc: MessagesControllerComponents
                                )(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  def getLessons: Action[AnyContent] = Action.async { implicit request =>
    repo.list().map { lessons => Ok(Json.toJson(lessons)) }
  }

  def uploadLessons(filename: String): Action[AnyContent] = Action.async { implicit request =>
    repo.removeAll().flatMap { _ => // all lessons successfully removed
      val parser = new Parser("/tmp/schedule/" + filename)
      repo.addAll(parser.parseLessons).map {
        _ => Ok(Json.toJson("done"))
      }

    }
  }
}
