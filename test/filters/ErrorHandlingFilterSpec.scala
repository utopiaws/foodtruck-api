package filters

import org.scalatestplus.play.PlaySpec
import play.api.http.HttpVerbs
import play.api.libs.json.Json

import play.api.mvc.{Results, Action, EssentialAction}
import play.api.test._
import play.api.test.Helpers._

class ErrorHandlingFilterSpec extends PlaySpec with Results {
  "ErrorHandlingFilter" should {
    "pass-through non-error responses" in {
      val okAction: EssentialAction = Action { request => Status(OK)("Everything is ok") }
      val result = call(ErrorHandlingFilter.apply(okAction), FakeRequest(HttpVerbs.GET, "/foo/bar"))

      status(result) mustEqual OK
      contentAsString(result) mustEqual "Everything is ok"
    }

    "handle java.util.concurrent.TimeoutException" in {
      val errorAction: EssentialAction = Action { request => throw new java.util.concurrent.TimeoutException() }
      val result = call(ErrorHandlingFilter.apply(errorAction), FakeRequest(HttpVerbs.GET, "/foo/bar"))

      status(result) mustEqual BAD_GATEWAY
      contentAsJson(result) mustEqual Json.obj("error" -> BAD_GATEWAY,
        "message" -> "Request to backend data.sfgov.org API server timed out")
    }

    "handle java.lang.NullPointerException" in {
      val errorAction: EssentialAction = Action {request => throw new java.lang.NullPointerException("null exception")}
      val result = call(ErrorHandlingFilter.apply(errorAction), FakeRequest(HttpVerbs.GET, "/foo/bar"))

      status(result) mustEqual BAD_GATEWAY
      contentAsJson(result) mustEqual Json.obj("error" -> BAD_GATEWAY,
        "message" -> "null exception")
    }

    "handle all other exceptions" in {
      val errorAction: EssentialAction = Action { request => throw new RuntimeException("FUBAR!") }
      val result = call(ErrorHandlingFilter.apply(errorAction), FakeRequest(HttpVerbs.GET, "/foo/bar"))

      status(result) mustEqual INTERNAL_SERVER_ERROR
      contentAsJson(result) mustEqual Json.obj("error" -> INTERNAL_SERVER_ERROR,
        "message" -> "java.lang.RuntimeException: FUBAR!")
    }
  }
}
