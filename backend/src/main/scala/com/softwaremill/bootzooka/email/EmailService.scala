package com.softwaremill.bootzooka.email

import com.softwaremill.bootzooka.infrastructure.Doobie._
import monix.eval.{Fiber, Task}
import cats.implicits._
import com.softwaremill.bootzooka.email.sender.EmailSender
import com.softwaremill.bootzooka.util.IdGenerator
import com.typesafe.scalalogging.StrictLogging

/**
  * Schedules emails to be sent asynchronously, in the background, as well as manages sending of emails in batches.
  */
class EmailService(idGenerator: IdGenerator, emailSender: EmailSender, config: EmailConfig, xa: Transactor[Task])
    extends EmailScheduler
    with StrictLogging {

  def apply(data: EmailData): ConnectionIO[Unit] = {
    logger.debug(s"Scheduling email to be sent to: ${data.recipient}")
    EmailModel.insert(Email(idGenerator.nextId(), data))
  }

  def sendBatch(): Task[Unit] = {
    for {
      emails <- EmailModel.find(config.batchSize).transact(xa)
      _ = if (emails.nonEmpty) logger.info(s"Sending ${emails.size} emails")
      _ <- Task.sequence(emails.map(_.data).map(emailSender.apply))
      _ <- EmailModel.delete(emails.map(_.id)).transact(xa)
    } yield ()
  }

  /**
    * Starts an asynchronous process which attempts to send batches of emails in defined intervals.
    */
  def startSender(): Task[Fiber[Nothing]] = {
    (sendBatch() >> Task.sleep(config.emailSendInterval))
      .onErrorHandle { e =>
        logger.error("Exception when sending emails", e)
      }
      .loopForever
      .start
  }
}

trait EmailScheduler {
  def apply(data: EmailData): ConnectionIO[Unit]
}