package probot

import java.io.IOException
import java.net.URL
import java.util.concurrent.TimeUnit

import com.google.common.util.concurrent.Uninterruptibles
import org.apache.http.client.utils.URIBuilder
import org.apache.juneau.microservice.Resource
import org.apache.juneau.rest.{RestException, RestRequest}
import org.apache.juneau.rest.annotation.{HtmlDoc, Properties, Property, Query, RestMethod, RestResource}
import org.apache.juneau.rest.widget.QueryMenuItem
import org.apache.streams.config.{ComponentConfigurator, StreamsConfigurator}
import org.apache.streams.twitter.{TwitterTimelineProviderConfiguration, TwitterUserInformationConfiguration}
import org.apache.streams.twitter.api.UsersLookupRequest
import org.apache.streams.twitter.pojo.{Tweet, User}
import org.apache.streams.twitter.provider.TwitterUserInformationProvider

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.util.Try

object TopPosts {

  val postEntityKeywords : List[String] = List("post", "tweet", "status")

  lazy val url : String = new URIBuilder(RootResource.asUri(StreamsConfigurator.getConfig().getConfig("server")))
    .setPath("/twitter/TopPosts").toString
}

@RestResource(
  defaultRequestHeaders = Array("Accept: text/html"),
  defaultResponseHeaders = Array("Content-Type: text/html"),
  htmldoc=new HtmlDoc(
    header=Array("Probot > Twitter > TopPosts"),
    footer=Array("ASF 2.0 License")
  ),
  path = "/TopPosts",
  title = "probot.TopPosts",
  description = "probot.TopPosts"
)
class TopPosts extends Resource {

  import TopPosts._

  @RestMethod(
    name = "GET"
    //    ,
    //    bpIncludes = "{User:'name'}"
  )
  @throws[IOException]
  def doGet(req: RestRequest) : java.util.List[TopPostItem] = {
    val metric = req.getQuery("metric")
    val tweets : List[Tweet] = TwitterResource.timeline
    var postList : List[TopPostItem] = {
      if( List("favs", "favorite", "favourites").exists(metric.contains) ) tweets.map(tweet => new TopPostItem().withCount(tweet.getFavoriteCount).withId(tweet.getId).withPost(tweet)).toList
      else if( List("shares", "retweets").exists(metric.contains) ) tweets.map(tweet => new TopPostItem().withCount(tweet.getRetweetCount).withId(tweet.getId).withPost(tweet)).toList
      else throw new RestException(400, "invalid metric")
    }
    val result = postList.sortBy(_.getCount.toLong)(Ordering.Long.reverse)
    result
  }

}