package probot


import java.io.IOException
import java.util
import java.util.concurrent.TimeUnit

import javax.servlet.ServletException
import akka.actor.{Actor, ActorRef, Props}
import com.fasterxml.jackson.core.JsonProcessingException
import com.google.common.util.concurrent.Uninterruptibles
import org.apache.http.HttpResponse
import org.apache.http.client.utils.URIBuilder
import org.apache.juneau.ObjectMap
import org.apache.juneau.rest.BasicRestServlet
import org.apache.juneau.rest.{RestRequest, RestResponse}
import org.apache.juneau.rest.annotation.{HtmlDoc, Property, RestMethod, RestResource}
import org.apache.juneau.rest.converters.{Introspectable, Queryable, Traversable}
import org.apache.streams.config.{ComponentConfigurator, StreamsConfiguration, StreamsConfigurator}
import org.apache.streams.twitter.api._
import org.apache.streams.twitter.config.{TwitterConfiguration, TwitterFollowingConfiguration, TwitterTimelineProviderConfiguration}
import org.apache.streams.twitter.pojo.{Follow, Tweet, User}
import org.apache.streams.twitter.provider.{TwitterFollowingProvider, TwitterTimelineProvider}

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

object ConfigurationResource {

  val serverConfig = StreamsConfigurator.getConfig().getConfig("server")

  val streams: StreamsConfiguration = StreamsConfigurator.detectConfiguration()
  val twitter: TwitterConfiguration = new ComponentConfigurator(classOf[TwitterConfiguration]).detectConfiguration(StreamsConfigurator.getConfig().getConfig("twitter"));
  val url : String = new URIBuilder(RootResource.asUri(serverConfig)).toString
  val welcomeMessage = StreamsConfigurator.getConfig.getString("welcome_message");

}

@RestResource(
  //  defaultRequestHeaders = Array("Accept: application/json", "Content-Type: application/json"),
  //  defaultResponseHeaders = Array("Content-Type: application/json"),
  htmldoc=new HtmlDoc(
    footer=Array("ASF 2.0 License"),
    header=Array("Probot > Configuration"),
    navlinks=Array("options: '?method=OPTIONS'")
  ),
  path = "/configuration",
  title = Array("probot.Configuration"),
  description = Array("probot.Configuration"),
  converters=Array(classOf[Traversable],classOf[Queryable],classOf[Introspectable]),
  properties=Array(new Property(name = "REST_allowMethodParam", value = "*"))
)
class ConfigurationResource extends BasicRestServlet {
  import ConfigurationResource._

  @RestMethod(name = "GET")
  @throws[IOException]
  def get(req: RestRequest, res: RestResponse) = {

    val objectMap = new ObjectMap()
      .append("streams", streams)
      .append("twitter", twitter)
      .append("url", url)
      .append("welcomeMessage", welcomeMessage)

    res.setOutput(objectMap)
    res.setStatus(200)

  }
}
