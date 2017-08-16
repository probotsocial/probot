package probot

import java.io.IOException
import java.net.URL

import com.sun.javafx.fxml.builder.URLBuilder
import com.typesafe.config.Config
import org.apache.http.client.utils.URIBuilder
import org.apache.juneau.microservice.Resource
import org.apache.juneau.rest.RestRequest
import org.apache.juneau.rest.annotation.{HtmlDoc, RestMethod, RestResource}
import org.apache.streams.config.StreamsConfigurator

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

object TopHashtags {
  lazy val url : String = new URIBuilder(RootResource.asUri(StreamsConfigurator.getConfig().getConfig("server")))
    .setPath("/twitter/top_hashtags").toString
}

@RestResource(
  defaultRequestHeaders = Array("Accept: text/html"),
  defaultResponseHeaders = Array("Content-Type: text/html"),
  htmldoc=new HtmlDoc(
    header=Array("Probot > Twitter > TopHashtags"),
    links=Array("options: '?method=OPTIONS'"),
    footer=Array("ASF 2.0 License")
  ),
  path = "/top_hashtags",
  title = "probot.TopHashtags",
  description = "probot.TopHashtags"
)
class TopHashtags extends Resource {

  @RestMethod(name = "GET")
  @throws[IOException]
    def doGet(req: RestRequest) : java.util.List[java.util.Map[String, String]] = {
    val hashtagIterator = TwitterResource.timeline.flatMap(tweet => tweet.getEntities.getHashtags.iterator())
    val hashtagCounts: List[(String, Int)] = hashtagIterator.map(_.getText.toLowerCase).groupBy(identity).mapValues(_.size).toSeq.sortBy(- _._2).toList
    var hashtagList : java.util.List[java.util.Map[String, String]] = hashtagCounts.map(x => Map("hashtag" -> x._1.toString, "count" -> x._2.toString).asJava).toList
    hashtagList
  }

}