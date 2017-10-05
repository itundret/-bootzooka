package com.softwaremill.bootzooka.user.domain

import com.softwaremill.bootzooka.common.crypto.{Argon2dPasswordHashing, CryptoConfig, PasswordHashing}
import com.typesafe.config.Config
import org.scalatest.{FlatSpec, Matchers}

class UserSpec extends FlatSpec with Matchers {
  implicit val hashing: PasswordHashing = new Argon2dPasswordHashing(new CryptoConfig {
    override def rootConfig: Config = ???
    override lazy val iterations = 2
    override lazy val memory = 16383
    override lazy val parallelism = 4
  })

  "encrypt password" should "take into account the password" in {
    // given
    val p1   = "pass1"
    val p2   = "pass2"
    val salt = "salt"

    // when
    val e1 = hashing.hashPassword(p1, salt)
    val e2 = hashing.hashPassword(p2, salt)

    // then
    info(s"$p1 encrypted is: $e1")
    info(s"$p2 encrypted is: $e2")

    e1.length should be >= (10)
    e2.length should be >= (10)

    e1 should not be (e2)
  }

  "encrypt password" should "take into account the salt" in {
    // given
    val pass  = "pass"
    val salt1 = "salt1"
    val salt2 = "salt2"

    // when
    val e1 = hashing.hashPassword(pass, salt1)
    val e2 = hashing.hashPassword(pass, salt2)

    // then
    info(s"$pass encrypted with $salt1 is: $e1")
    info(s"$pass encrypted with $salt2 is: $e2")

    e1.length should be >= (10)
    e2.length should be >= (10)

    e1 should not be (e2)
  }
}
