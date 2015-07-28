package filters

import java.text.SimpleDateFormat
import java.util.Calendar

import play.api.Logger
import play.api.mvc.{Filter, RequestHeader, Result}

import scala.concurrent.Future

/*
  AccessLogingFilter is used to store ping logs into access.log. It is used to check the log info and find the
  status error and bugs
 */
object AccessLoggingFilter extends Filter {
  import play.api.libs.concurrent.Execution.Implicits.defaultContext  // execution context for Futures

  val accessLogger = Logger("access")

  def apply(next: (RequestHeader) => Future[Result])(request: RequestHeader): Future[Result] = {
    val startTime = System.currentTimeMillis
    val resultFuture = next(request)

    resultFuture.map { result =>
      val endTime = System.currentTimeMillis
      val requestTime = (endTime - startTime).toDouble / 1000
      val datetime = getCurrentTime
      val contentLength = result.header.headers.get("Content-Length").getOrElse("0")
      val remoteAddress = getRemoteAddress(request)
      val msg = s"${remoteAddress}, ${request.domain} - - [${datetime}] " +
        s"""\"${request.method} ${request.uri} ${request.version}\"""" + " " + result.header.status + " " +
        contentLength + " " + requestTime
      accessLogger.info(msg)
      result
    }
  }

  def getCurrentTime : String = {
    val now = Calendar.getInstance().getTime()
    new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss").format(now)
  }

  def getRemoteAddress (request: RequestHeader): String = {
    request.headers.get("X-Forwarded-For") match {
      case Some(oldValue) if oldValue != null && oldValue.nonEmpty => oldValue.split(",")(0)
      case _ => request.remoteAddress
    }
  }
}
