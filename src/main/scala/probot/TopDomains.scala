package probot

import java.io.IOException
import java.net.URL

import org.apache.http.client.utils.URIBuilder
import org.apache.juneau.microservice.Resource
import org.apache.juneau.rest.RestRequest
import org.apache.juneau.rest.annotation.{HtmlDoc, RestMethod, RestResource}
import org.apache.streams.config.StreamsConfigurator

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.util.Try

object TopDomains {
  lazy val url : String = new URIBuilder(RootResource.asUri(StreamsConfigurator.getConfig().getConfig("server")))
    .setPath("/twitter/top_domains").toString

  def domainOf( url : String ) : Option[String] = {
    Try(new URL(url).getHost).toOption
  }
}

@RestResource(
  defaultRequestHeaders = Array("Accept: text/html"),
  defaultResponseHeaders = Array("Content-Type: text/html"),
  htmldoc=new HtmlDoc(
    aside=""
      + "<div style='max-width:400px;min-width:200px'>"
      + "Probot.TopDomains"
      + "</div>",
    nav="probot > Twitter > TopDomains",
    title="Probot.TopDomains",
    description="Probot.TopDomains",
    header="Probot.TopDomains",
    footer="ASF 2.0 License"
  ),
  path = "/top_domains",
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