package models

import play.api.libs.json._

case class User(id: Long, name: String, username: String, password: String, isAdmin: Boolean, studentId: Option[String],
                group: Option[String])

object User {
  implicit val userFormat: OFormat[User] = Json.format[User]
}
