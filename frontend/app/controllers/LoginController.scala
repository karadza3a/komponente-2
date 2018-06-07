package controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import play.api.mvc._
import services.session.SessionService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LoginController @Inject()(userAction: UserInfoAction,
                                sessionGenerator: SessionGenerator,
                                sessionService: SessionService,
                                cc: ControllerComponents,
                                ws: WSClient
                               )(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def login: Action[AnyContent] = userAction.async { implicit request: UserRequest[AnyContent] =>
    val successFunc = { userInfo: UserInfo =>
      sessionGenerator.createSession(userInfo).map {
        case (sessionId, encryptedCookie) =>
          val session = request.session + (SESSION_ID -> sessionId)
          Redirect(routes.HomeController.index())
            .withSession(session)
            .withCookies(encryptedCookie)
      }
    }

    val (username, password) = userForm.bindFromRequest.get

    ws.url("http://localhost:9090/login")
      .addQueryStringParameters("username" -> username, "password" -> password)
      .get().flatMap(response =>
      response.status match {
        case 200 =>
          successFunc(UserInfo(
            (response.json \ "username").as[String],
            (response.json \ "id").as[Long],
            (response.json \ "isAdmin").as[Boolean]
          ))
        case _ => Future.successful(Redirect(routes.HomeController.index()).flashing(FLASH_ERROR -> "Could not login!"))
      }
    )
  }

  def logout = Action { implicit request: Request[AnyContent] =>
    // When we delete the session id, removing the secret key is enough to render the
    // user info cookie unusable.
    request.session.get(SESSION_ID).foreach { sessionId =>
      sessionService.delete(sessionId)
    }

    discardingSession {
      Redirect(routes.HomeController.index())
    }
  }

}
