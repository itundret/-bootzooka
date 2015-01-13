package uitest.pages

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.FindBy
import uitest.commands.SeleniumCommands

class MessagesPage(driver: WebDriver) {
  private val sc: SeleniumCommands = new SeleniumCommands(driver)

  @FindBy(css = "#logoutLink")
  val logoutLink: WebElement = null

  @FindBy(css = ".alert-info")
  val infoAlert: WebElement = null

  @FindBy(css = ".alert-danger")
  val errorAlert: WebElement = null

  @FindBy(css = "textarea")
  val messageField: WebElement = null

  @FindBy(css = "input[type=submit]")
  val sendButton: WebElement = null

  def logout() {
    logoutLink.click()
    sc.waitForFinishLoading()
  }

  def sendMessage(messagetext: String) {
    messageField.sendKeys(messagetext)
    sendButton.click()
    sc.waitForFinishLoading()
  }

  def isUserLogged(user: String): Boolean = {
    sc.waitForElementVisible(By.linkText("Logged in as " + user))
    true
  }

  def isUMessageDisplayed(message: String): Boolean = {
    sc.waitForElementVisible(By.xpath("//p[text()='" + message + "']"))
    true
  }

  def getInfoText: String = {
    sc.waitForElementVisible(By.cssSelector("#info-message"))
    infoAlert.getText
  }

  def getErrorText: String = {
    sc.waitForElementVisible(By.cssSelector("#error-message"))
    errorAlert.getText
  }
}
