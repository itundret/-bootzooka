package com.softwaremill.bootzooka.passwordreset

import java.time.Instant

import cats.implicits._

import com.softwaremill.bootzooka.Id
import com.softwaremill.bootzooka.user.User
import com.softwaremill.tagging.@@
import com.softwaremill.bootzooka.infrastructure.Doobie._
import com.softwaremill.bootzooka.security.AuthTokenOps

object PasswordResetCodeModel {

  def insert(pr: PasswordResetCode): ConnectionIO[Unit] = {
    sql"""INSERT INTO password_reset_codes (id, user_id, valid_until)
         |VALUES (${pr.id}, ${pr.userId}, ${pr.validUntil})""".stripMargin.update.run.void
  }

  def delete(id: Id @@ PasswordResetCode): ConnectionIO[Unit] = {
    sql"""DELETE FROM password_reset_codes WHERE id = $id""".stripMargin.update.run.void
  }

  def findById(id: Id @@ PasswordResetCode): ConnectionIO[Option[PasswordResetCode]] = {
    sql"SELECT id, user_id, valid_until FROM password_reset_codes WHERE id = $id"
      .query[PasswordResetCode]
      .option
  }
}

case class PasswordResetCode(id: Id @@ PasswordResetCode, userId: Id @@ User, validUntil: Instant)

object PasswordResetAuthToken extends AuthTokenOps[PasswordResetCode] {
  override def tokenName: String = "PasswordResetCode"
  override def findById: Id @@ PasswordResetCode => ConnectionIO[Option[PasswordResetCode]] = PasswordResetCodeModel.findById
  override def delete: PasswordResetCode => ConnectionIO[Unit] = ak => PasswordResetCodeModel.delete(ak.id)
  override def userId: PasswordResetCode => Id @@ User = _.userId
  override def validUntil: PasswordResetCode => Instant = _.validUntil
  override def deleteWhenValid: Boolean = true
}
