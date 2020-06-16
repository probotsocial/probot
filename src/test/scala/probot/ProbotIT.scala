import java.net.URI
import java.util.{Locale, Properties}

import javax.ws.rs.core.Response
import com.google.common.base.Preconditions
import com.typesafe.config.{Config, ConfigValue}
import org.apache.http.HttpResponse
import org.apache.juneau.ObjectMap
import org.apache.juneau.config.ConfigBuilder
import org.apache.juneau.html.HtmlParser
import org.apache.juneau.json.JsonParser
import org.apache.juneau.microservice.jetty.JettyMicroservice
import org.apache.juneau.microservice.jetty.JettyMicroserviceBuilder
import org.apache.juneau.microservice.resources.ConfigResource
import org.apache.juneau.microservice.resources.LogsResource
import org.apache.juneau.rest.client.{RestCall, RestClient, RestClientBuilder}
import org.apache.juneau.rest.helper.ResourceDescription
import org.apache.streams.config.StreamsConfigurator
import org.apache.streams.twitter.pojo.{DirectMessageEvent, WebhookEvents}
import org.slf4j.{Logger, LoggerFactory}
import org.testng.Assert
import org.testng.Assert.assertEquals
import org.testng.annotations.BeforeSuite
import org.testng.annotations.{AfterClass, BeforeClass, Test}
import probot.ProbotMicroservice
import probot.ProbotResource

import scala.collection.JavaConversions._

class ProbotMicroserviceITs {

  val LOGGER = LoggerFactory.getLogger(classOf[ProbotMicroserviceITs])

  var microservice : JettyMicroservice = _
  var configFile : Config = _
  var microserviceURI : URI = _
  var restClientBuilder : RestClientBuilder = _
  var restClient : RestClient = _

  /**
    * Startup the probot service.
    */
  @BeforeSuite
  @throws[Exception]
  def setUp(): Unit = {
    Locale.setDefault(Locale.US)
    val configFilePath = "target/test-classes/application.cfg"
    val builder : JettyMicroserviceBuilder = JettyMicroservice
      .create()
      .args("microservice.cfg")
      .servlet(classOf[ConfigResource])
      .servlet(classOf[LogsResource])
      .servlet(classOf[ProbotResource])
    microservice = new ProbotMicroservice(builder)
    microservice.start()
    LOGGER.info("microservice: {}", microservice)
    microserviceURI = microservice.getURI
    LOGGER.info("microserviceURI: {}", microserviceURI)
    restClientBuilder = RestClient.create().rootUrl(microserviceURI)
    restClient = restClientBuilder.build
  }

  /**
    * Shutdown the probot service.
    */
  @AfterClass
  @throws[Exception]
  def tearDown(): Unit = {
    microservice.stop
    restClient.closeQuietly()
  }

  /**
    * Confirm probot service online.
    */
  @Test(groups = Array("online"))
  @throws[Exception]
  def testMicroserviceHtml() = {
    val client = restClientBuilder.parser(HtmlParser.DEFAULT).accept("text/html+stripped").build
    val r = client.doGet("")
    val x = r.getResponse(classOf[Array[ResourceDescription]])
    assertEquals(1, x.length)
    client.closeQuietly()
  }

  /**
    * assert CRC returns and is correct.
    */
  @Test(groups = Array("security"), dependsOnGroups = Array("online"))
  @throws[Exception]
  def testCRC() = {
    val client = restClientBuilder.parser(JsonParser.DEFAULT).accept("application/json").build
    val call = client.doGet("/twitter/webhook").query("crc_token", "probot")
    val response = call.getResponse(classOf[ObjectMap])
    Assert.assertNotNull(response)
    Assert.assertNotNull(response.getString("response_token"))
    Assert.assertTrue(response.getString("response_token").startsWith("sha256="))
    Assert.assertTrue(response.getString("response_token").endsWith("="))
  }

  /**
    * assert CRC returns and is correct.
    */
  @Test(groups = Array("security"), dependsOnGroups = Array("online"))
  @throws[Exception]
  def testInvalidHeaderSignature() = {
    val client = restClientBuilder.parser(JsonParser.DEFAULT).accept("application/json").build
    val event = new DirectMessageEvent()
    val body = new WebhookEvents().withDirectMessageEvents(List(event))
    val call = client.doPost("/twitter/webhook", body)
      .header("x-twitter-webhooks-signature", "probot")
    val response = call.getResponse()
    Assert.assertNotNull(response)
    Assert.assertEquals(response.getStatusLine.getStatusCode, Response.Status.BAD_REQUEST)
  }

//  /**
//    * assert welcome message configured
//    */
//  def testSetupWelcomeMessage()
//
//  /**
//    * assert subscription configured
//    */
//  def testSetupSubscription()
//
//  /**
//    * assert account settings loaded
//    */
//  def testAccountSettingsLoaded()
//
//  /**
//    * assert account activity loaded
//    */
//  def testAccountActivityLoaded()

}