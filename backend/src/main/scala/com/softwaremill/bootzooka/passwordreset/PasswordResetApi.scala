package com.softwaremill.bootzooka.passwordreset

import cats.data.NonEmptyList
import com.softwaremill.bootzooka.ServerEndpoints
import com.softwaremill.bootzooka.infrastructure.Http
import com.softwaremill.bootzooka.infrastructure.Json._

class PasswordResetApi(http: Http, passwordResetService: PasswordResetService) {
  import PasswordResetApi._
  import http._

  private val PasswordReset = "passwordreset"

  private val passwordResetEndpoint = baseEndpoint.post
    .in(PasswordReset / "reset")
    .in(jsonBody[PasswordReset_IN])
    .out(jsonBody[PasswordReset_OUT])
    .serverLogic { data =>
      (for {
        _ <- passwordResetService.resetPassword(data.code, data.password)
      } yield PasswordReset_OUT()).toOut
    }

  private val forgotPasswordEndpoint = baseEndpoint.post
    .in(PasswordReset / "forgot")
    .in(jsonBody[ForgotPassword_IN])
    .out(jsonBody[ForgotPassword_OUT])
    .serverLogic { data =>
      (for {
        _ <- passwordResetService.forgotPassword(data.loginOrEmail).transact
      } yield ForgotPassword_OUT()).toOut
    }

  val endpoints: ServerEndpoints =
    NonEmptyList
      .of(
        passwordResetEndpoint,
        forgotPasswordEndpoint
      )
}

object PasswordResetApi {
  case class PasswordReset_IN(code: String, password: String)
  case class PasswordReset_OUT()

  case class ForgotPassword_IN(loginOrEmail: String)
  case class ForgotPassword_OUT()
}
