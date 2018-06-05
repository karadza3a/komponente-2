package controllers

import java.io.FileNotFoundException
import java.nio.file.Paths

import javax.inject._
import play.api.libs.Files
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Random

@Singleton
class AdminController @Inject()(userAction: UserInfoAction,
                                cc: ControllerComponents,
                                ws: WSClient) extends AbstractController(cc) {

  def index = userAction { implicit request: UserRequest[_] =>
    Ok(views.html.admin(form))
  }

  def upload: Action[MultipartFormData[Files.TemporaryFile]] = Action.async(parse.multipartFormData) { request =>
    val eventualResponse: Future[WSResponse] = request.body.file("schedule").map { schedule =>

      val filename = Random.alphanumeric.take(10).mkString
      schedule.ref.moveTo(Paths.get(s"/tmp/schedule/$filename"), replace = true)

      ws.url("http://localhost:9091/lessons/upload/" + filename)
        .addHttpHeaders("Accept" -> "application/json").get()
    } match {
      case Some(response) => response
      case None => Future.failed(new FileNotFoundException("Schedule file not found."))
    }

    eventualResponse.map { response =>
      response.status match {
        case 200 =>
          Redirect(routes.AdminController.index()).flashing("success" -> "Schedule updated!")
        case _ =>
          Redirect(routes.AdminController.index()).flashing("error" -> response.statusText)
      }
    }.recover { case error: Exception =>
      Redirect(routes.AdminController.index()).flashing("error" -> error.getLocalizedMessage)
    }
  }
}
