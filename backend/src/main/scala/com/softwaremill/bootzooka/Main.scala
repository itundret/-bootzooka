package com.softwaremill.bootzooka

import com.softwaremill.bootzooka.config.Config
import com.softwaremill.bootzooka.infrastructure.CorrelationId
import com.softwaremill.bootzooka.metrics.Metrics
import com.typesafe.scalalogging.StrictLogging
import doobie.util.transactor
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import sttp.client.SttpBackend

object Main extends StrictLogging {
  def main(args: Array[String]): Unit = {
    CorrelationId.init()
    Metrics.init()
    Thread.setDefaultUncaughtExceptionHandler((t, e) => logger.error("Uncaught exception in thread: " + t, e))

    val initModule = new InitModule {}
    initModule.logConfig()

    val mainTask = initModule.db.transactorResource.use { _xa =>
      initModule.baseSttpBackend.use { _baseSttpBackend =>
        val modules = new MainModule {
          override def xa: transactor.Transactor[Task] = _xa
          override def baseSttpBackend: SttpBackend[Task, Nothing, Nothing] = _baseSttpBackend
          override def config: Config = initModule.config
        }

        /*
        Sequencing two tasks using the >> operator:
        - the first starts the background processes (such as an email sender)
        - the second is a stream of handling requests, compiled to a single task which never ends (unless the socket
          is broken)
         */
        modules.startBackgroundProcesses >> modules.httpApi.serveRequests.compile.drain
      }
    }
    mainTask.runSyncUnsafe()
  }
}
