package controllers

import java.nio.file.Paths

import javax.inject._
import play.api.libs.Files
import play.api.mvc._

import scala.util.Random

@Singleton
class AdminController @Inject()(userAction: UserInfoAction,
                                cc: ControllerComponents) extends AbstractController(cc) {

  def index = userAction { implicit request: UserRequest[_] =>
    Ok(views.html.admin(form))
  }

  def upload: Action[MultipartFormData[Files.TemporaryFile]] = Action(parse.multipartFormData) { request =>
    request.body.file("schedule").map { schedule =>

      // only get the last part of the filename
      // otherwise someone can send a path like ../../home/foo/bar.txt to write to other files on the system
      val filename = Random.alphanumeric.take(10).mkString
      schedule.ref.moveTo(Paths.get(s"/tmp/schedule/$filename"), replace = true)

      Ok("File uploaded")
    }.getOrElse {
      Redirect(routes.AdminController.index()).flashing("error" -> "Missing file")
    }
  }

}
