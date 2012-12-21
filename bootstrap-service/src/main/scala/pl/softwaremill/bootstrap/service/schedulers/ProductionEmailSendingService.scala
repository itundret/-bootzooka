package pl.softwaremill.bootstrap.service.schedulers

import pl.softwaremill.common.sqs.email.EmailSender
import pl.softwaremill.bootstrap.service.config.BootstrapConfiguration._
import pl.softwaremill.common.sqs.util.EmailDescription
import javax.mail.MessagingException
import pl.softwaremill.common.sqs.{ ReceivedMessage, Queue, SQS }
import com.google.common.base.Optional
import scala.util.control.Breaks._

class ProductionEmailSendingService extends EmailSendingService {

  val sqsClient = new SQS("queue.amazonaws.com", awsAccessKeyId, awsSecretAccessKey)
  val emailQueue: Queue = sqsClient.getQueueByName(taskSQSQueue)

  def run() {
    var messageOpt: Optional[ReceivedMessage] = emailQueue.receiveSingleMessage

    breakable {
      logger.debug("Checking emails waiting in the Amazon SQS")

      while (messageOpt.isPresent) {
        val message: ReceivedMessage = messageOpt.get()
        val emailToSend: EmailDescription = message.getMessage.asInstanceOf[EmailDescription]

        try {
          EmailSender.send(smtpHost, smtpPort, smtpUserName, smtpPassword, from, encoding, emailToSend)
          logger.info("Email sent!")
          emailQueue.deleteMessage(message)
        } catch {
          case e: MessagingException =>
            logger.error("Sending email failed: " + e.getMessage)
            break()
        }

        messageOpt = emailQueue.receiveSingleMessage
      }
    }
  }

  def scheduleEmail(address: String, subject: String, content: String) {
    emailQueue.sendSerializable(new EmailDescription(address, content, subject))
  }
}
