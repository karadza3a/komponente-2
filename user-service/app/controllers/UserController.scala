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

  val studentForm: Form[CreateStudentForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "username" -> nonEmptyText,
      "password" -> nonEmptyText,
      "studentId" -> nonEmptyText,
      "group" -> nonEmptyText,
    )(CreateStudentForm.apply)(CreateStudentForm.unapply)
  }

  def addStudent(): Action[AnyContent] = Action.async { implicit request =>
    studentForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(InternalServerError(errorForm.errors.toString))
      },
      student => {
        repo.createStudent(student.name, student.username, student.password, student.studentId, student.group).map { s =>
          Ok(s.id.toString)
        }
      }
    )
  }

  def getUsers: Action[AnyContent] = Action.async { implicit request =>
    repo.list().map { users =>
      Ok(Json.toJson(users))
    }
  }

  def getUserById(id: Long): Action[AnyContent] = Action.async { implicit request =>
    repo.getUserById(id).map {
      case Some(user) => Ok(Json.toJson(user))
      case None => NotFound("User not found.")
    }
  }

  def login(username: String, password: String): Action[AnyContent] = Action.async { implicit request =>
    repo.getUserByCredentials(username, password).map {
      case Some(user) => Ok(Json.toJson(user))
      case None => NotFound("User not found.")
    }
  }
}

case class CreateStudentForm(name: String, username: String, password: String, studentId: String, group: String)
