package probot

import java.net.{URI, URL}
import javax.servlet.ServletException

import com.typesafe.config.Config
import org.apache.http.client.utils.URIBuilder
import org.apache.juneau.microservice.ResourceGroup
import org.apache.juneau.rest.annotation.{HtmlDoc, RestResource}
import org.apache.streams.config.StreamsConfigurator

object RootResource {
  lazy val url : String = asUri(StreamsConfigurator.getConfig().getConfig("server")).toString

  def asUri(config : Config) : URI = {
    val uri = new URIBuilder()
        .setScheme(config.getString("scheme"))
        .setHost(config.getString("host"))
        .build()
    uri
  }

}
@RestResource(
  defaultRequestHeaders = Array("Accept: application/json", "Content-Type: application/json"),
  //defaultResponseHeaders = Array("Content-Type: application/json"),
  htmldoc=new HtmlDoc(
    aside=""
      + "<div style='max-width:400px;min-width:200px'>"
      + "Probot.Home"
      + "</div>",
    nav="probot",
    title="Probot",
    description="Probot.Home",
    header="Probot",
    footer="ASF 2.0 License"
  ),
  //  messages = "messages.properties",
  path = "/",
  title = "Probot",
  description = "Probot"
  ,
  children = Array(
    classOf[probot.TwitterResource]
  )
)
class RootResource extends ResourceGroup {

  @throws[ServletException]
  override def init(): Unit = {
    this.log("init")
  }

}
