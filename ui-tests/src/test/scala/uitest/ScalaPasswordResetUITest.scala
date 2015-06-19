package uitest

import com.softwaremill.bootzooka.domain.PasswordResetCode
import org.fest.assertions.Assertions._

import scala.concurrent.ExecutionContext.Implicits.global

class ScalaPasswordResetUITest extends BaseUITest {

  private val validCode = "SOME00CODE"
  private val invalidCode = validCode + "666"

  override def beforeAll(): Unit = {
    super.beforeAll()
    registerUserIfNotExists("someUser", "some-user@example.com", "somePass") foreach { _ =>
      beans.userDao.findByLoginOrEmail("someUser").futureValue foreach { user =>
        val passResetCode = PasswordResetCode(validCode, user)
        beans.codeDao.add(passResetCode).futureValue
      }
    }
  }

  override def afterAll(): Unit = {
    beans.codeDao.load(validCode).map(codeOpt => codeOpt.foreach(beans.codeDao.remove)).futureValue
    removeUsers("someUser")
    super.afterAll()
  }

  test("password-reset should reset password") {
    passwordRestPage.openPasswordResetPage(validCode)
    passwordRestPage.resetPassword("asd", "asd")

    assertThat(messagesPage.getInfoText.contains("Your password has been changed")).isTrue()
  }

  test("password-reset should not reset password due to missing code") {
    passwordRestPage.openPasswordResetPage("")
    passwordRestPage.resetPassword("asd", "asd")

    assertThat(messagesPage.getErrorText.contains("Wrong or malformed password recovery code.")).isTrue()
  }

  test("password-reset should not reset password due to invalid code") {
    passwordRestPage.openPasswordResetPage(invalidCode)
    passwordRestPage.resetPassword("asd", "asd")

    assertThat(messagesPage.getErrorText.contains("Your reset code is invalid. Please try again.")).isTrue()
  }

  test("password-reset should do nothing if password & its repetition differ") {
    passwordRestPage.openPasswordResetPage(validCode)
    passwordRestPage.resetPassword("asd", "notMatching")
    assertThat(passwordRestPage.getErrorText.contains("Passwords don't match!")).isTrue()
  }


}
