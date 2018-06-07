package controllers

import javax.inject._
import play.api.libs.ws.WSClient
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class HomeController @Inject()(userAction: UserInfoAction,
                               cc: ControllerComponents,
                               ws: WSClient
                              ) extends AbstractController(cc) {

  def index = userAction { implicit request: UserRequest[_] =>
    Ok(views.html.index(userForm))
  }

  def register = userAction { implicit request: UserRequest[_] =>
    Ok(views.html.register(studentForm))
  }

  def doRegister(): Action[AnyContent] = userAction.async { implicit request: UserRequest[AnyContent] =>
    val (name, username, password, studentId, group) = studentForm.bindFromRequest.get
    ws.url("http://localhost:9090/student")
      .post(Map(
        "name" -> Seq(name),
        "username" -> Seq(username),
        "password" -> Seq(password),
        "studentId" -> Seq(studentId),
        "group" -> Seq(group)
      )).map { response =>
      response.status match {
        case 200 =>
          Redirect(routes.HomeController.index()).flashing("success" -> "Registration successful!")
        case _ =>
          Redirect(routes.HomeController.register()).flashing("error" -> "There was an error.")
      }
    }
  }
}
