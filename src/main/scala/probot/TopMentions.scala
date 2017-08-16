package probot

import java.io.IOException
import java.net.URL
import java.util.concurrent.TimeUnit

import com.google.common.util.concurrent.Uninterruptibles
import org.apache.http.client.utils.URIBuilder
import org.apache.juneau.microservice.Resource
import org.apache.juneau.rest.RestRequest
import org.apache.juneau.rest.annotation.{HtmlDoc, Properties, Property, RestMethod, RestResource}
import org.apache.juneau.rest.widget.QueryMenuItem
import org.apache.streams.config.{ComponentConfigurator, StreamsConfigurator}
import org.apache.streams.twitter.{TwitterTimelineProviderConfiguration, TwitterUserInformationConfiguration}
import org.apache.streams.twitter.api.UsersLookupRequest
import org.apache.streams.twitter.pojo.User
import org.apache.streams.twitter.provider.TwitterUserInformationProvider

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.util.Try

object TopMentions {
  lazy val url : String = new URIBuilder(RootResource.asUri(StreamsConfigurator.getConfig().getConfig("server")))
    .setPath("/twitter/top_mentions").toString

  lazy val userInformationConfiguration: TwitterUserInformationConfiguration = new ComponentConfigurator(classOf[TwitterUserInformationConfiguration]).detectConfiguration(StreamsConfigurator.getConfig().getConfig("twitter"));

  lazy val mentionIterator = TwitterResource.timeline.flatMap(tweet => tweet.getEntities.getUserMentions.iterator())
  lazy val mentionIds : List[Long] = mentionIterator.map(mention => mention.getId.toLong)
  lazy val mentionUsers : List[User] = mentionedUsers(mentionIds)

  def mentionedUsers(mentionIds : List[Long]) : List[User] = {
    val userBuffer = scala.collection.mutable.ArrayBuffer.empty[User]
    val userInformationProviderConfiguration = userInformationConfiguration
      .withInfo(mentionIds.map(_.toString).asJava)
    var userInformationProvider = new TwitterUserInformationProvider(userInformationProviderConfiguration)
    userInformationProvider.prepare(userInformationProviderConfiguration)
    userInformationProvider.startStream()

    do {
      Uninterruptibles.sleepUninterruptibly(TwitterResource.streamsConfiguration.getBatchFrequencyMs, TimeUnit.MILLISECONDS)
      import scala.collection.JavaConversions._
      for (datum <- userInformationProvider.readCurrent) {
        userBuffer += datum.getDocument.asInstanceOf[User]
      }
    } while ( {
      userInformationProvider.isRunning
    })
    userInformationProvider.cleanUp()

    userBuffer.toList
  }
}

@RestResource(
  defaultRequestHeaders = Array("Accept: text/html"),
  defaultResponseHeaders = Array("Content-Type: text/html"),
  htmldoc=new HtmlDoc(
    header=Array("Probot > Twitter > TopMentions"),
    links=Array("options: '?method=OPTIONS'"),
    footer=Array("ASF 2.0 License")
  ),
  path = "/top_mentions",
  title = "probot.TopMentions",
  description = "probot.TopMentions"
)
class TopMentions extends Resource {

  import TopMentions._

  @RestMethod(
    name = "GET"
//    ,
//    bpIncludes = "{User:'name'}"
  )
  @throws[IOException]
  def doGet(req: RestRequest) : java.util.List[TopMentionItem] = {
    val userMap: Map[Long, User] = mentionUsers map (_.getId.toLong) zip mentionUsers toMap
    val countMap: Map[Long, Int] = mentionIterator.map(mention => mention.getId.longValue()).groupBy(identity).mapValues(_.size).toSeq.sortBy(- _._2) toMap
    val mapsAsList = countMap.toList ++ userMap.toList
    val join: Map[Long, List[Any]] = mapsAsList.groupBy(_._1).map{case(k,v) => k -> v.map(_._2)}
    val mentionList : List[TopMentionItem] = join.flatMap(
      x => {
        val id = x._1
        val countItem = x._2.filter(_.isInstanceOf[Int]).toList
        val userItem = x._2.filter(_.isInstanceOf[User]).toList
        val countOption = if( countItem.size > 0 ) Some(countItem.get(0).asInstanceOf[Int]) else None
        val userOption = if( userItem.size > 0 ) Some(userItem.get(0).asInstanceOf[User]) else None
        if( countOption.isDefined && userOption.isDefined ) {
          var item: TopMentionItem = new TopMentionItem
          item.setId(id)
          item.setCount(countOption.get.toLong)
          item.setUser(userOption.get)
          Some(item)
        } else {
          None
        }
      }
    ).toList
    mentionList.asJava
  }

}