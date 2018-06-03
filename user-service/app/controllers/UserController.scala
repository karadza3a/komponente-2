package controllers

import javax.inject._
import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class UserController @Inject()(repo: UserRepository,
                               cc: MessagesControllerComponents
                              )(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  /**
    * The mapping for the user form.
    */
  val studentForm: Form[CreateStudentForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "username" -> nonEmptyText,
      "password" -> nonEmptyText,
      "studentId" -> nonEmptyText,
      "group" -> nonEmptyText,
    )(CreateStudentForm.apply)(CreateStudentForm.unapply)
  }

  /**
    * The index action.
    */
  def index = Action { implicit request =>
    Ok(views.html.index(studentForm))
  }

  /**
    * The add person action.
    *
    * This is asynchronous, since we're invoking the asynchronous methods on PersonRepository.
    */
  def addStudent(): Action[AnyContent] = Action.async { implicit request =>
    // Bind the form first, then fold the result, passing a function to handle errors, and a function to handle succes.
    studentForm.bindFromRequest.fold(
      // The error function. We return the index page with the error form, which will render the errors.
      // We also wrap the result in a successful future, since this action is synchronous, but we're required to return
      // a future because the person creation function returns a future.
      errorForm => {
        Future.successful(Ok(views.html.index(errorForm)))
      },
      // There were no errors in the from, so create the person.
      student => {
        repo.createStudent(student.name, student.username, student.password, student.studentId, student.group).map { _ =>
          // If successful, we simply redirect to the index page.
          Redirect(routes.UserController.index()).flashing("success" -> "user.created")
        }
      }
    )
  }

  /**
    * A REST endpoint that gets all the people as JSON.
    */
  def getUsers: Action[AnyContent] = Action.async { implicit request =>
    repo.list().map { users =>
      Ok(Json.toJson(users))
    }
  }
}

/**
  * The create person form.
  *
  * Generally for forms, you should define separate objects to your models, since forms very often need to present data
  * in a different way to your models.  In this case, it doesn't make sense to have an id parameter in the form, since
  * that is generated once it's created.
  */
case class CreateStudentForm(name: String, username: String, password: String, studentId: String, group: String)
