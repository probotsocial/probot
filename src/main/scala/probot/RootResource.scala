package probot

import java.net.{URI, URL}
import javax.servlet.ServletException

import akka.actor.ActorSystem

import com.typesafe.config.Config
import org.apache.http.client.utils.URIBuilder
import org.apache.juneau.microservice.ResourceGroup
import org.apache.juneau.rest.annotation.{HtmlDoc, RestResource}
import org.apache.streams.config.StreamsConfigurator

object RootResource {

  val system = ActorSystem("mySystem")

  val url : String = asUri(StreamsConfigurator.getConfig().getConfig("server")).toString

  def asUri(config : Config) : URI = {
    val uri = new URIBuilder()
        .setScheme(config.getString("scheme"))
        .setHost(config.getString("host"))
        .build()
    uri
  }

}
@RestResource(
  htmldoc=new HtmlDoc(
    aside=Array(
      "<div style='max-width:400px;min-width:200px'>",
      "Probot.Home",
      "</div>"),
    footer=Array("ASF 2.0 License")
  ),
  path = "/",
  title = "Probot",
  description = "Probot"
)
class RootResource extends ResourceGroup {

}
