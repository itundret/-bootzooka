package com.softwaremill.bootzooka.test

import java.time.{OffsetDateTime, ZoneOffset}

import com.softwaremill.bootzooka.user.User

import scala.util.Random

trait TestHelpers {

  val createdOn = OffsetDateTime.of(2015, 6, 3, 13, 25, 3, 0, ZoneOffset.UTC)

  def randomString(length: Int = 10) = Stream.continually(random.nextInt(characters.length)).map(characters).take(length).mkString
  private val random = new scala.util.Random
  private val characters = "abcdefghijklmnopqrstuvwxyz0123456789"

  def newUser(login: String, email: String, pass: String, salt: String): User =
    User.withRandomUUID(login, email, pass, salt, createdOn)

  def newRandomUser(password: Option[String] = None): User = {
    val login = randomString()
    val pass = password.getOrElse(randomString())
    newUser(login, s"$login@example.com", pass, "someSalt")
  }
}
