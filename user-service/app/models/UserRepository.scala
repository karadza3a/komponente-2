package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{Future, ExecutionContext}

/**
  * A repository for people.
  *
  * @param dbConfigProvider The Play db config provider. Play will inject this for you.
  */
@Singleton
class UserRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import profile.api._

  /**
    * Here we define the table. It will have a name of people
    */
  private class UserTable(tag: Tag) extends Table[User](tag, "users") {

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def username = column[String]("username")

    def password = column[String]("password")

    def isAdmin = column[Boolean]("is_admin")

    def studentId = column[Option[String]]("student_id")

    def group = column[Option[String]]("student_group")

    /**
      * This is the tables default "projection".
      *
      * It defines how the columns are converted to and from the Person object.
      *
      * In this case, we are simply passing the id, name and page parameters to the Person case classes
      * apply and unapply methods.
      */
    def * = (id, name, username, password, isAdmin, studentId, group) <> ((User.apply _).tupled, User.unapply)
  }

  /**
    * The starting point for all queries on the people table.
    */
  private val users = TableQuery[UserTable]

  /**
    * Create a student user
    *
    * This is an asynchronous operation, it will return a future of the created user, which can be used to obtain the
    * id for that user.
    */
  def createStudent(name: String, username: String, password: String, studentId: String,
                    group: String): Future[User] = db.run {
    // We create a projection of all columns but the id, since we're not inserting a value for the id column
    (users.map(u => (u.name, u.username, u.password, u.isAdmin, u.studentId, u.group))
      // Now define it to return the id, because we want to know what id was generated for the user
      returning users.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the
      // returned id
      into ((map, id) => {
      User(id, map._1, map._2, map._3, isAdmin = false, map._5, map._6)
    })
      // And finally, insert the user into the database
      ) += (name, username, password, false, Some(studentId), Some(group))
  }

  /**
    * Create an admin user
    *
    * This is an asynchronous operation, it will return a future of the created user, which can be used to obtain the
    * id for that user.
    */
  def createAdmin(name: String, username: String, password: String): Future[User] = db.run {
    (users.map(u => (u.name, u.username, u.password, u.isAdmin))
      returning users.map(_.id)
      into ((map, id) => User(id, map._1, map._2, map._3, isAdmin = true, None, None))
      ) += (name, username, password, true)
  }

  /**
    * List all the users in the database.
    */
  def list(): Future[Seq[User]] = db.run {
    users.result
  }

  def getUserById(id: Long): Future[Option[User]] = db.run {
    users.filter(_.id === id).result.map(result => result.size match {
      case 1 => Some(result.last)
      case _ => None
    })
  }

  def getUserByCredentials(username: String, password: String): Future[Option[User]] = db.run {
    users
      .filter(_.username === username)
      .filter(_.password === password)
      .result.map(
      result => result.size match {
        case 1 => Some(result.last)
        case _ => None
      })
  }
}
