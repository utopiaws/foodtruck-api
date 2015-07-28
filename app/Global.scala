import filters._
import play.api.mvc.WithFilters
import play.api.{Logger, Application}

// Note: filters are applied to requests in the order that they are defined here, and to responses in the
// reverse order. So with 2 filters f1 and f2, defined as "... WithFilters(f1, f2)", the request/response
// flow is:
//   request: client -> f1 -> f2 -> application
//   response: application -> f2 -> f1 -> client
//
// Of course, a filter will only see responses if it does something with the result of calling next(rh),
// which many filters do not do.
//
// There are 2 filters called AccessLoggingFilter, ErrorHandlingFilter.
// This ensures that we log all requests, and translate all exceptions thrown during
// request handling into 5xx HTTP errors.
// Also, if have more time, I should add one called dataDogFilter, which send stats to DataDog
object Global extends WithFilters(
   AccessLoggingFilter,   // Logs all requests into access.log
   ErrorHandlingFilter
) {
  override def onStart(app: Application) {
    Logger.info("Play application has started")
  }

  override def onStop(app: Application) {
    Logger.info("Play application has stopped")
  }
}
