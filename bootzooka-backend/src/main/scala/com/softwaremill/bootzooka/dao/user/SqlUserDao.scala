package com.softwaremill.bootzooka.dao.user

import com.softwaremill.bootzooka.dao.sql.SqlDatabase
import com.softwaremill.bootzooka.domain.User

import scala.concurrent.{ExecutionContext, Future}
import com.softwaremill.bootzooka.common.FutureHelpers._

class SqlUserDao(protected val database: SqlDatabase)(implicit val ec: ExecutionContext)
  extends UserDao with SqlUserSchema {

  import database._
  import database.driver.api._

  override def loadAll() = db.run(users.result)

  override protected def internalAddUser(user: User): Future[Unit] = {
    db.run(users += user).mapToUnit
  }

  override def remove(userId: UserId): Future[Unit] = {
    db.run(users.filter(_.id === userId).delete).mapToUnit
  }

  override def load(userId: UserId): Future[Option[User]] =
    findOneWhere(_.id === userId)

  private def findOneWhere(condition: Users => Rep[Boolean]): Future[Option[User]] = {
    db.run(users.filter(condition).result.headOption)
  }

  override def findByEmail(email: String)  =
    findOneWhere(_.email.toLowerCase === email.toLowerCase)

  override def findByLowerCasedLogin(login: String) =
    findOneWhere(_.loginLowerCase === login.toLowerCase)

  override def findByLoginOrEmail(loginOrEmail: String) = {
    findByLowerCasedLogin(loginOrEmail).flatMap(userOpt =>
      userOpt.map(user => Future{Some(user)}).getOrElse(findByEmail(loginOrEmail))
    )
  }

  override def findForIdentifiers(uniqueIds: Set[UserId]): Future[Seq[User]] = {
      db.run(users.filter(_.id inSet uniqueIds).result)
  }

  override def findByToken(token: String) =
    findOneWhere(_.token === token)

  override def changePassword(userId: UserId, newPassword: String): Future[Unit] = {
    db.run(users.filter(_.id === userId).map(_.password).update(newPassword)).mapToUnit
  }

  override def changeLogin(currentLogin: String, newLogin: String): Future[Unit] = {
    val action = users.filter(_.loginLowerCase === currentLogin.toLowerCase).map { user =>
      (user.login, user.loginLowerCase)
    }.update((newLogin, newLogin.toLowerCase))
    db.run(action).mapToUnit
  }

  override def changeEmail(currentEmail: String, newEmail: String): Future[Unit] = {
    db.run(users.filter(_.email.toLowerCase === currentEmail.toLowerCase).map(_.email).update(newEmail)).mapToUnit
  }
}
