package controllers

import javax.inject._
import play.api.mvc._

@Singleton
class StudentController @Inject()(userAction: UserInfoAction,
                                  cc: ControllerComponents) extends AbstractController(cc) {

  def index = userAction { implicit request: UserRequest[_] =>
    Ok(views.html.sync(form))
  }

}
