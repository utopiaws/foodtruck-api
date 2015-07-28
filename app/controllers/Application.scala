package controllers


import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar

import play.api.Play
import play.api.Play.current
import play.api.http.HttpVerbs
import play.api.libs.json.{JsArray, Json}
import play.api.libs.ws.{WS, WSResponse}
import play.api.mvc._

import scala.concurrent.Future

object Application extends Controller {
  import play.api.libs.concurrent.Execution.Implicits.defaultContext  // execution context for Futures

  lazy val SourceApiHost = Play.current.configuration.getString("foodtruck.source.api.url").get
  lazy val SourceApiTimeoutMillis = Play.current.configuration.getInt("foodtruck.source.api.timeout-millis").getOrElse(15000)


  /*
    getNearByFoodTruck returns the nearest food truck according to set conditions. It should exist two parameters in uri,
    they are longtitude and latitude, which are the users' location. User can set the range of truck through "within", and
    keyword, which users can input keyword contained in the truck's whole info.
  */
  def getNearByFoodTruck = Action.async(parse.raw) { implicit request =>
    val limit = request.getQueryString("limit").getOrElse("10")
    val longti = request.getQueryString("longitude").getOrElse(null)
    val lati = request.getQueryString("latitude").getOrElse(null)
    val range = request.getQueryString("within").getOrElse("1000")
    val status = URLEncoder.encode("'APPROVED'", "UTF-8")
    val keyword = request.getQueryString("keyword").getOrElse(null)

    val urlPath = getfilterDatePath + URLEncoder.encode(" AND ", "UTF-8") + "within_circle(location," + URLEncoder.encode(lati, "UTF-8") + "," +
                  URLEncoder.encode(longti, "UTF-8") + "," + URLEncoder.encode(range, "UTF-8") + ")" + "&" + "status=" + status +
                  "&" + "$limit=" + limit
    val responseFuture = getResponseFuture(SourceApiHost, urlPath, request)

    keyword match {
      case null => forwardResponseToClient(responseFuture)
      case _ => filterByKeyWord(responseFuture, keyword)
    }
  }

  /*
    The following four are used to complete exact search. If user set exact location/address/objectid/status, api server
    should return exact results.
  */
  def searchByLocation = Action.async(parse.raw) { implicit request =>
    val longti = request.getQueryString("longitude").getOrElse(null)
    val lati = request.getQueryString("latitude").getOrElse(null)

    if(longti == null || lati == null) {
      throw new java.lang.NullPointerException("you should input longitude= and latitude= for location search")
    }
    val urlPath = "longitude=" + URLEncoder.encode(longti, "UTF-8") + "&" +
                  "latitude=" + URLEncoder.encode(lati, "UTF-8") + "&" + getfilterDatePath
    val responseFuture = getResponseFuture(SourceApiHost, urlPath, request)

    forwardResponseToClient(responseFuture)
  }

  def searchByStatus = Action.async(parse.raw) { implicit request =>
    val status = request.getQueryString("status").getOrElse(null)

    if(status == null) {
      throw new java.lang.NullPointerException("you should input status= for status search")
    }
    val urlPath = "status=" + status + "&" + getfilterDatePath
    val responseFuture = getResponseFuture(SourceApiHost, urlPath, request)

    forwardResponseToClient(responseFuture)
  }

  def searchByObjectid = Action.async(parse.raw) { implicit request =>
    val objectId = request.getQueryString("objectid").getOrElse(null)
    if(objectId == null) {
      throw new java.lang.NullPointerException("you should input objectid= for objectId search")
    }
    val urlPath = "objectid=" + objectId + "&" + getfilterDatePath
    val responseFuture = getResponseFuture(SourceApiHost, urlPath, request)

    forwardResponseToClient(responseFuture)
  }

  def searchByAddress = Action.async(parse.raw) { implicit request =>
    val address = request.getQueryString("address").getOrElse(null)
    if(address == null) {
      throw new java.lang.NullPointerException("you should input address= for address search")
    }
    val urlPath = "address=" + URLEncoder.encode(address,"UTF-8") + "&" + getfilterDatePath
    val responseFuture = getResponseFuture(SourceApiHost, urlPath, request)

    forwardResponseToClient(responseFuture)
  }

  //All the return results should not be expired. We use current time as condition, all the truck's expiration date should
  // be after current date
  def getfilterDatePath: String = {
    val curDate = Calendar.getInstance().getTime
    val formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss")
    val dateAndTime = formatter.format(curDate)

    val filterPath = "$where=expirationdate" +  URLEncoder.encode(">" + "'" + dateAndTime + "'", "UTF-8")
    filterPath
  }

  // get response from source api server
  def getResponseFuture(hostname: String, urlPath: String, request: Request[RawBuffer]): Future[WSResponse] = {
    val res = request.method match {
      case HttpVerbs.GET | HttpVerbs.HEAD | HttpVerbs.DELETE | HttpVerbs.OPTIONS =>
        WS.url(SourceApiHost + "?" + urlPath).
          withRequestTimeout(SourceApiTimeoutMillis).
          execute(request.method)
      case HttpVerbs.PUT | HttpVerbs.POST | HttpVerbs.PATCH =>
        val requestBodyBytes = request.body.asBytes().getOrElse(Array[Byte]())

        WS.url(SourceApiHost + "?" + urlPath).
          withRequestTimeout(SourceApiTimeoutMillis).
          withBody(requestBodyBytes).
          execute(request.method)
    }
    res
  }

  //filter the generated response through keyword
  def filterByKeyWord(responseFuture: Future[WSResponse], keyword: String): Future[Result] = responseFuture.map { response =>

    val contents = Json.parse(response.body).as[JsArray].value
    val result = contents.filter(ele => ele.toString().contains(keyword))

    Status(response.status)(JsArray.apply(result))
  }

  def forwardResponseToClient(responseFuture: Future[WSResponse]): Future[Result] = responseFuture.map { response =>
    Status(response.status)(response.body)
  }
}
