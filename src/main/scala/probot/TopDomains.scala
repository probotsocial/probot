package probot

import java.io.IOException
import java.net.URL

import org.apache.http.client.utils.URIBuilder
import org.apache.juneau.microservice.Resource
import org.apache.juneau.rest.RestRequest
import org.apache.juneau.rest.annotation.{HtmlDoc, RestMethod, RestResource}
import org.apache.juneau.rest.widget.QueryMenuItem
import org.apache.streams.config.StreamsConfigurator

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import scala.util.Try

object TopDomains {

  val domainEntityKeywords = List("domain", "site", "web")

  lazy val url : String = new URIBuilder(RootResource.asUri(StreamsConfigurator.getConfig().getConfig("server")))
    .setPath("/twitter/TopDomains").toString

  def domainOf( url : String ) : Option[String] = {
    Try(new URL(url).getHost).toOption
  }
}

@RestResource(
  defaultRequestHeaders = Array("Accept: text/html"),
  defaultResponseHeaders = Array("Content-Type: text/html"),
  htmldoc=new HtmlDoc(
    header=Array("Probot > Twitter > TopDomains"),
    links=Array("options: '?method=OPTIONS'"),
    footer=Array("ASF 2.0 License")
  ),
  path = "/TopDomains",
  title = "probot.TopDomains",
  description = "probot.TopDomains"
)
class TopDomains extends Resource {

  import TopDomains._

  @RestMethod(name = "GET")
  @throws[IOException]
  def doGet(req: RestRequest) : java.util.List[java.util.Map[String, String]] = {
    val urlIterator = TwitterResource.timeline.flatMap(tweet => tweet.getEntities.getUrls.iterator())
    val expandedUrlIterator = urlIterator.map(url => url.getExpandedUrl)
    val domainIterator = expandedUrlIterator.flatMap(expanded_url => domainOf(expanded_url))
    val domainCounts: List[(String, Int)] = domainIterator.groupBy(identity).mapValues(_.size).toSeq.sortBy(- _._2).toList
    var domainList : java.util.List[java.util.Map[String, String]] = domainCounts.map(x => Map("domain" -> x._1.toString, "count" -> x._2.toString).asJava).toList
    domainList
  }

}