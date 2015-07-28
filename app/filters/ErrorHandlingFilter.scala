package filters

import play.api.Logger
import play.api.http.{ContentTypes, Status}
import play.api.libs.json.Json
import play.api.mvc._

/*
  ErrorHandlingFilter is used in Global. When it generated exceptions, api should return the messages according to different
  exceptions
 */
object ErrorHandlingFilter extends EssentialFilter with Results with Status with ContentTypes {
  import play.api.libs.concurrent.Execution.Implicits.defaultContext  // execution context for Futures

  val logger = Logger("application")
//"invalid input for longitude and latitude, you should setup longitude and latitude"
  override def apply(next: EssentialAction): EssentialAction = new EssentialAction {
    def apply(rh: RequestHeader) = next(rh).recover {
      case e: java.util.concurrent.TimeoutException =>
        logger.error(rh.toString + ": " + e.toString)
        Status(BAD_GATEWAY)(Json.obj("error" -> BAD_GATEWAY,
          "message" -> "Request to backend data.sfgov.org API server timed out")).as(JSON)
      case e: java.lang.NullPointerException =>
        logger.error(rh.toString + ": " + e.toString)
        Status(BAD_GATEWAY)(Json.obj("error" -> BAD_GATEWAY,
          "message" -> e.getMessage)).as(JSON)
      case e: Throwable =>
        logger.error(rh.toString + ": " + e.toString)
        Status(INTERNAL_SERVER_ERROR)(Json.obj("error" -> INTERNAL_SERVER_ERROR,
          "message" -> e.toString)).as(JSON)
    }
  }
}
