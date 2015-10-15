package com.softwaremill.bootzooka.api

import com.softwaremill.bootzooka.version.BuildInfo._
import akka.http.scaladsl.server.Directives._
import io.circe.generic.auto._

trait VersionRoutes extends RoutesSupport {

  val versionRoutes = path("version") {
    complete {
      VersionJson(buildSha, buildDate)
    }
  }
}

case class VersionJson(build: String, date: String)
