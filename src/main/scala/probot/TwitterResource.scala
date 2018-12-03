package probot


import java.io.IOException
import java.util
import java.util.concurrent.TimeUnit
import javax.servlet.ServletException

import akka.actor.{Actor, ActorRef, Props}
import com.fasterxml.jackson.core.JsonProcessingException
import com.google.common.util.concurrent.Uninterruptibles
import org.apache.http.HttpResponse
import org.apache.juneau.ObjectMap
import org.apache.juneau.microservice.{Resource, ResourceGroup}
import org.apache.juneau.rest.{RestRequest, RestResponse}
import org.apache.juneau.rest.annotation.{HtmlDoc, Property, RestMethod, RestResource}
import org.apache.juneau.rest.converters.{Introspectable, Queryable, Traversable}
import org.apache.juneau.rest.remoteable.RemoteableServlet
import org.apache.streams.config.{ComponentConfigurator, StreamsConfiguration, StreamsConfigurator}
import org.apache.streams.twitter.{TwitterConfiguration, TwitterFollowingConfiguration, TwitterTimelineProviderConfiguration}
import org.apache.streams.twitter.api._
import org.apache.streams.twitter.pojo.{Follow, Tweet, User}
import org.apache.streams.twitter.provider.{TwitterFollowingProvider, TwitterTimelineProvider}

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

object TwitterResource {

  val twitter: org.apache.streams.twitter.api.Twitter = Twitter.getInstance(ConfigurationResource.twitter)
  val twitterSecurity: org.apache.streams.twitter.api.TwitterSecurity = new TwitterSecurity

  final val accountSettings: AccountSettings = twitter.settings()
  lazy final val followers: List[User] = following(accountSettings.getScreenName, ConfigurationResource.followers)
  lazy final val friends: List[User] = following(accountSettings.getScreenName, ConfigurationResource.friends)
  lazy final val timeline: List[Tweet] = posts(accountSettings.getScreenName)
  lazy final val user: User = twitter.verifyCredentials()

  lazy val messageCreateRequestConsumer: ActorRef = RootResource.system.actorOf(Props[MessageCreateRequestConsumer])

  def posts(screenname : String) : List[Tweet] = {
    val timelineBuffer = scala.collection.mutable.ArrayBuffer.empty[Tweet]
    val timelineProviderConfiguration = ConfigurationResource.timeline
      .withInfo(List(screenname))
      .asInstanceOf[TwitterTimelineProviderConfiguration]
    val timelineProvider = new TwitterTimelineProvider(timelineProviderConfiguration)
    timelineProvider.prepare(timelineProviderConfiguration)
    timelineProvider.startStream()

    do {
      Uninterruptibles.sleepUninterruptibly(ConfigurationResource.streams.getBatchFrequencyMs, TimeUnit.MILLISECONDS)
      import scala.collection.JavaConversions._
      for (datum <- timelineProvider.readCurrent) {
        timelineBuffer += datum.getDocument.asInstanceOf[Tweet]
      }
    } while ( {
      timelineProvider.isRunning
    })
    timelineProvider.cleanUp()

    timelineBuffer.toList
  }

  def following(screenname : String, baseConfiguration : TwitterFollowingConfiguration) : List[User] = {
    val buffer = scala.collection.mutable.ArrayBuffer.empty[User]
    val configuration = baseConfiguration
      .withInfo(List(screenname))
      .asInstanceOf[TwitterFollowingConfiguration]
    val provider = new TwitterFollowingProvider(configuration)
    provider.prepare(configuration)
    provider.startStream()

    do {
      Uninterruptibles.sleepUninterruptibly(ConfigurationResource.streams.getBatchFrequencyMs, TimeUnit.MILLISECONDS)
      import scala.collection.JavaConversions._
      for( datum <- provider.readCurrent ) {
        val follow = datum.getDocument.asInstanceOf[Follow]
        val user = {
          if( configuration.getEndpoint.equals("friends"))
            follow.getFollowee
          else
            follow.getFollower
        }
        buffer += user
      }
    } while ( {
      provider.isRunning
    })
    provider.cleanUp()

    buffer.toList
  }
}

@RestResource(
  //  defaultRequestHeaders = Array("Accept: application/json", "Content-Type: application/json"),
  //  defaultResponseHeaders = Array("Content-Type: application/json"),
  htmldoc=new HtmlDoc(
    header=Array("Probot > Twitter"),
    footer=Array("ASF 2.0 License")
  ),
  path = "/twitter",
  title = "probot.Twitter",
  description = "probot.Twitter",
  converters=Array(classOf[Traversable],classOf[Queryable],classOf[Introspectable]),
  properties=Array(new Property(name = "REST_allowMethodParam", value = "*")),
  children = Array(
    classOf[probot.TopDomains],
    classOf[probot.TopFollowing],
    classOf[probot.TopHashtags],
    classOf[probot.TopMentions],
    classOf[probot.TopPosts],
    classOf[probot.WebhookResource]
  )
)
class TwitterResource extends Resource {
  import TwitterResource._

  @RestMethod(name = "GET")
  @throws[IOException]
  def get(req: RestRequest,
          res: RestResponse) = {

    val objectMap = new ObjectMap()
      .append("followers", followers.size)
      .append("friends", friends.size)
      .append("timeline", timeline.size)
      .append("user", user)

    res.setOutput(objectMap)
    res.setStatus(200)

  }
}
