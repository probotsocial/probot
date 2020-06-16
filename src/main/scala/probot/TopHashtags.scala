package probot

import java.io.IOException
import java.net.URL

import org.apache.http.client.utils.URIBuilder
import org.apache.juneau.rest.BasicRestServlet
import org.apache.juneau.rest.RestRequest
import org.apache.juneau.rest.annotation.{HtmlDoc, RestMethod, RestResource}
import org.apache.streams.config.StreamsConfigurator

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

object TopHashtags {

  val hashtagEntityKeywords = List("hashtag")

  lazy val url : String = new URIBuilder(RootResource.asUri(StreamsConfigurator.getConfig().getConfig("server")))
    .setPath("/twitter/TopHashtags").toString
}

@RestResource(
  defaultRequestHeaders = Array("Accept: text/html"),
  defaultResponseHeaders = Array("Content-Type: text/html"),
  htmldoc=new HtmlDoc(
    header=Array("Probot > Twitter > TopHashtags"),
    footer=Array("ASF 2.0 License")
  ),
  path = "/TopHashtags",
  title = Array("probot.TopHashtags"),
  description = Array("probot.TopHashtags")
)
class TopHashtags extends BasicRestServlet {

  @RestMethod(name = "GET")
  @throws[IOException]
    def doGet(req: RestRequest) : java.util.List[java.util.Map[String, String]] = {
    val hashtagIterator = TwitterResource.timeline.flatMap(tweet => tweet.getEntities.getHashtags.iterator())
    val hashtagCounts: List[(String, Int)] = hashtagIterator.map(_.getText.toLowerCase).groupBy(identity).mapValues(_.size).toSeq.sortBy(- _._2).toList
    var hashtagList : java.util.List[java.util.Map[String, String]] = hashtagCounts.map(x => Map("hashtag" -> x._1.toString, "count" -> x._2.toString).asJava).toList
    hashtagList
  }

}