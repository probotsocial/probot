package social.probot.microservice

import java.io.IOException

import org.apache.http.client.utils.URIBuilder
import org.apache.juneau.ObjectMap
import org.apache.juneau.rest.BasicRestServlet
import org.apache.juneau.rest.annotation.HtmlDoc
import org.apache.juneau.rest.annotation.Property
import org.apache.juneau.rest.annotation.RestMethod
import org.apache.juneau.rest.annotation.RestResource
import org.apache.juneau.rest.converters.Introspectable
import org.apache.juneau.rest.converters.Queryable
import org.apache.juneau.rest.converters.Traversable
import org.apache.juneau.rest.RestRequest
import org.apache.juneau.rest.RestResponse
import org.apache.streams.config.ComponentConfigurator
import org.apache.streams.config.StreamsConfiguration
import org.apache.streams.config.StreamsConfigurator
import org.apache.streams.twitter.config.TwitterConfiguration

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
