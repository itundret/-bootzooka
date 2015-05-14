package uitest

import com.softwaremill.bootzooka.domain.PasswordResetCode
import org.fest.assertions.Assertions

import scala.concurrent.ExecutionContext

class ScalaPasswordResetUITest(implicit ec: ExecutionContext) extends BootzookaUITest {

  private val validCode = "SOME00CODE"
  private val invalidCode = validCode + "666"

  override def beforeAll(): Unit = {
    super.beforeAll()
    registerUserIfNotExists("someUser", "some-user@example.com", "somePass") foreach { _ =>
      beans.userDao.findByLoginOrEmail("someUser").map { userOpt =>
        userOpt foreach { user =>
        val passResetCode = PasswordResetCode(validCode, user)
        beans.codeDao.store(passResetCode).futureValue
        }
      }.futureValue
    }
  }

  override def afterAll(): Unit = {
    beans.codeDao.load(validCode).map(codeOpt => codeOpt.foreach(beans.codeDao.delete)).futureValue
    removeUsers("someUser")
    super.afterAll()
  }

  test("password-reset should reset password") {
    passwordRestPage.openPasswordResetPage(validCode)
    passwordRestPage.resetPassword("asd", "asd")

    Assertions.assertThat(messagesPage.getInfoText.contains("Your password has been changed"))
  }

  test("password-reset should not reset password due to missing code") {
    passwordRestPage.openPasswordResetPage("")
    passwordRestPage.resetPassword("asd", "asd")

    Assertions.assertThat(messagesPage.getErrorText.contains("Wrong or malformed password recovery code."))
  }

  test("password-reset should not reset password due to invalid code") {
    passwordRestPage.openPasswordResetPage(invalidCode)
    passwordRestPage.resetPassword("asd", "asd")

    Assertions.assertThat(messagesPage.getErrorText.contains("Wrong or malformed password recovery code."))
  }

  test("password-reset should do nothing if password & its repetition differ") {
    passwordRestPage.openPasswordResetPage(validCode)
    passwordRestPage.resetPassword("asd", "notMatching", sc => {
      Assertions.assertThat(messagesPage.isUMessageDisplayed("Passwords don't match!"))
    })
  }


}
