package probot

import java.util.UUID
import java.util.logging.Level

import org.apache.commons.lang3.StringUtils
import org.apache.juneau.json.JsonSerializerBuilder
import org.apache.juneau.microservice.Resource
import org.apache.juneau.rest.{RestRequest, RestResponse}
import org.apache.streams.pojo.json.Activity
import org.apache.streams.pojo.json.objectTypes.Application
import org.joda.time.DateTime

object ProbotResource {
  val defaultMsg = "{0}"
  val eventMsg = "{0} {1}"
  val exceptionMsg = "{0} {1}"
  val requestMsg = "{0} {1}"
  val responseMsg = "{0} {1}"

  val logSerializer = new JsonSerializerBuilder()
    .abridged(true)
    .detectRecursions(true)
    .ignoreRecursions(true)
    .build()

  def generateRequestId(req: RestRequest) : String = {
    UUID.randomUUID().toString;
  }
}

class ProbotResource extends Resource {

  import ProbotResource._

  override def onPreCall(req : RestRequest) = {
    var xrequestid: String = req.getHeader("X-Request-Id")
    if (StringUtils.isBlank(xrequestid)) {
      xrequestid = generateRequestId(req)
      req.getHeaders.put("X-Request-Id", xrequestid)
    }
    //val requestJson = logSerializer.serialize()
    log(Level.INFO, requestMsg, xrequestid, req.toString)
  }

  override def onPostCall(req : RestRequest, res : RestResponse) = {
    val xrequestid : String = req.getHeader("X-Request-Id")
    res.setHeader("X-Request-Id", xrequestid)
    //val responseJson = logSerializer.serialize()
    log(Level.INFO, responseMsg, xrequestid, res.getStatus.toString)
  }

  def baseActivity(req : RestRequest) : Activity = new Activity()
    .withId(req.getHeader("X-Request-Id"))
    .withPublished(DateTime.now())
}
