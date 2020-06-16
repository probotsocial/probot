package probot

import java.io.IOException
import java.net.URL

import org.apache.http.client.utils.URIBuilder
import org.apache.juneau.rest.BasicRestServlet
import org.apache.juneau.rest.annotation._
import org.apache.juneau.rest.{RestException, RestRequest, RestResponse}
import org.apache.streams.config.StreamsConfigurator
import org.apache.streams.twitter.pojo.User

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.util.Try

object TopFollowing {

  val followingEntityKeywords = List("follower", "friend")

  lazy val url : String = new URIBuilder(RootResource.asUri(StreamsConfigurator.getConfig().getConfig("server")))
    .setPath("/twitter/TopFollowing").toString
}

@RestResource(
  defaultRequestHeaders = Array("Accept: text/html"),
  defaultResponseHeaders = Array("Content-Type: text/html"),
  htmldoc=new HtmlDoc(
    header=Array("Probot > Twitter > TopFollowing"),
    footer=Array("ASF 2.0 License")
  ),
  path = "/TopFollowing",
  title = Array("probot.TopFollowing"),
  description = Array("probot.TopFollowing")
)
class TopFollowing extends BasicRestServlet {

  @RestMethod(name = "GET")
  @throws[Exception]
  def doGet(req: RestRequest) : java.util.List[java.util.Map[String, String]] = {
    val endpoint = req.getQuery("endpoint")
    val metric = req.getQuery("metric")
    val users : List[User] = {
      if( endpoint.contains("friend")) TwitterResource.friends
      else if( endpoint.contains("follower")) TwitterResource.followers
      else throw new RestException(400, "invalid endpoint")
    }
    var userList : List[(java.lang.Long, String)] = {
      if( List("favs", "favorite", "favourites").exists(metric.contains) ) users.map(user => (user.getFavouritesCount, user.getScreenName)).toList
      else if( List("followers").exists(metric.contains) ) users.map(user => (user.getFollowersCount, user.getScreenName)).toList
      else if( List("friends").exists(metric.contains) ) users.map(user => (user.getFriendsCount, user.getScreenName)).toList
      else if( List("list").exists(metric.contains) ) users.map(user => (user.getListedCount, user.getScreenName)).toList
      else if( List("status", "tweet", "post", "count").exists(metric.contains) ) users.map(user => (user.getStatusesCount, user.getScreenName)).toList
      else throw new RestException(400, "invalid metric")
    }
    val result : java.util.List[java.util.Map[String, String]] = userList.sortBy(_._1.toLong)(Ordering.Long.reverse)
      .map(x => Map(metric -> x._1.toString, "screenname" -> x._2.toString).asJava)
    result
  }

}