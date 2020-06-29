package probot

import java.util.UUID
import java.util.logging.Level

import org.apache.commons.lang3.StringUtils
import org.apache.juneau.json.JsonSerializerBuilder
import org.apache.juneau.rest.BasicRestServlet
import org.apache.juneau.rest.BasicRestServletGroup
import org.apache.juneau.rest.annotation.HtmlDoc
import org.apache.juneau.rest.annotation.RestResource
import org.apache.juneau.rest.annotation.{HookEvent, RestHook}
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
    .detectRecursions(true)
    .ignoreRecursions(true)
    .build()

  def generateRequestId(req: RestRequest) : String = {
    UUID.randomUUID().toString;
  }
}

@RestResource(
  defaultRequestHeaders = Array("Accept: text/html"),
  defaultResponseHeaders = Array("Content-Type: text/html"),
  htmldoc=new HtmlDoc(
    header=Array("Probot"),
    footer=Array("ASF 2.0 License")
  ),
  path = "/probot",
  title = Array("probot"),
  description = Array("probot"),
  children = Array(
    classOf[TwitterResource]
  )
)
class ProbotResource extends BasicRestServletGroup {

  import ProbotResource._

  @RestHook(HookEvent.PRE_CALL)
  def onPreCall(req : RestRequest) = {
    var xrequestid = req.getHeader("X-Request-Id")
    if (StringUtils.isBlank(xrequestid)) {
      xrequestid = generateRequestId(req)
      req.getHeaders.put("X-Request-Id", xrequestid)
    }
  }

  @RestHook(HookEvent.POST_CALL)
  def onPostCall(req : RestRequest, res : RestResponse) = {
    val xrequestid = req.getHeader("X-Request-Id")
    res.setHeader("X-Request-Id", xrequestid)
  }

  def baseActivity(req : RestRequest) : Activity = new Activity()
    .withId(req.getHeader("X-Request-Id"))
    .withPublished(DateTime.now())
}
