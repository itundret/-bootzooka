/*
 * COPYRIGHT (c) 2016 VOCADO, LLC.  ALL RIGHTS RESERVED.  THIS SOFTWARE CONTAINS
 * TRADE SECRETS AND/OR CONFIDENTIAL INFORMATION PROPRIETARY TO VOCADO, LLC AND/OR
 * ITS LICENSORS. ACCESS TO AND USE OF THIS INFORMATION IS STRICTLY LIMITED AND
 * CONTROLLED BY VOCADO, LLC.  THIS SOFTWARE MAY NOT BE COPIED, MODIFIED, DISTRIBUTED,
 * DISPLAYED, DISCLOSED OR USED IN ANY WAY NOT EXPRESSLY AUTHORIZED BY VOCADO, LLC IN WRITING.
 */

package com.softwaremill.bootzooka.common

import scala.concurrent.{ExecutionContext, Future}

object FutureHelpers {
  implicit class PimpedFuture[T](future: Future[T])(implicit val ec: ExecutionContext) {
    def mapToUnit = future.map(_ => ())
  }
}
