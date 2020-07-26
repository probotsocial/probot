package probot

import java.net.{URI, URL}

import javax.servlet.ServletException
import akka.actor.ActorSystem
import com.typesafe.config.Config
import org.apache.http.client.utils.URIBuilder
import org.apache.juneau.rest.BasicRestServletGroup
import org.apache.juneau.rest.annotation.{HtmlDoc, RestResource}
import org.apache.streams.config.StreamsConfigurator

object RootResource {

  val system = ActorSystem("mySystem")

  val url : String = asUri(StreamsConfigurator.getConfig().getConfig("server")).toString

  def asUri(config : Config) : URI = {
    val uri = new URIBuilder()
        .setScheme(config.getString("scheme"))
        .setUserInfo(config.getString("username"), config.getString("password"))
        .setHost(config.getString("host"))
        .setPort(config.getInt("port"))
        .setPath(config.getString("path"))
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
  title = Array("Home"),
  description = Array("Home"),
  children = Array(
    classOf[ConfigurationResource],
    classOf[ProbotResource]
  )
)
class RootResource extends BasicRestServletGroup {

}
