package com.softwaremill.bootzooka.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler, Route}
import com.softwaremill.bootzooka.passwordreset.PasswordResetRoutes
import com.softwaremill.bootzooka.user.UsersRoutes
import com.typesafe.scalalogging.StrictLogging

trait Routes extends RoutesBase
    with UsersRoutes
    with PasswordResetRoutes
    with VersionRoutes {

  lazy val routes = base {
    pathPrefix("api") {
      passwordResetRoutes ~
        usersRoutes ~
        versionRoutes
    } ~
      getFromResourceDirectory("webapp") ~
      path("") {
        getFromResource("webapp/index.html")
      }
  }
}

trait RoutesBase extends CacheSupport
    with SecuritySupport
    with StrictLogging {

  private val exceptionHandler = ExceptionHandler {
    case e: Exception =>
      logger.error(s"Exception during client request processing: ${e.getMessage}", e)
      _.complete(StatusCodes.InternalServerError, "Internal server error")
  }

  private val rejectionHandler = RejectionHandler.default
  private val logDuration = extractRequestContext.flatMap { ctx =>
    val start = System.currentTimeMillis()
    // handling rejections here so that we get proper status codes
    mapResponse { resp =>
      val d = System.currentTimeMillis() - start
      logger.info(s"[${resp.status.intValue()}] ${ctx.request.method.name} ${ctx.request.uri} took: ${d}ms")
      resp
    } & handleRejections(rejectionHandler)
  }

  def base(route: Route) =
    logDuration {
      handleExceptions(exceptionHandler) {
        (cacheImages & addSecurityHeaders) {
          encodeResponse {
            route
          }
        }
      }
    }
}
