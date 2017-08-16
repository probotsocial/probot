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
import org.apache.streams.twitter.{TwitterConfiguration, TwitterTimelineProviderConfiguration}
import org.apache.streams.twitter.api._
import org.apache.streams.twitter.pojo.{Tweet, User}
import org.apache.streams.twitter.provider.TwitterTimelineProvider

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

object TwitterResource {

  lazy val streamsConfiguration: StreamsConfiguration = StreamsConfigurator.detectConfiguration()
  lazy val twitterConfiguration: TwitterConfiguration = new ComponentConfigurator(classOf[TwitterConfiguration]).detectConfiguration(StreamsConfigurator.getConfig().getConfig("twitter"));
  lazy val twitter: org.apache.streams.twitter.api.Twitter = Twitter.getInstance(twitterConfiguration)
  lazy val twitterSecurity: org.apache.streams.twitter.api.TwitterSecurity = new TwitterSecurity

  lazy val timelineConfiguration: TwitterTimelineProviderConfiguration = new ComponentConfigurator(classOf[TwitterTimelineProviderConfiguration]).detectConfiguration(StreamsConfigurator.getConfig().getConfig("twitter"));

  lazy val accountSettings: AccountSettings = twitter.settings()
  lazy val timeline: List[Tweet] = posts(accountSettings.getScreenName)
  lazy val user: User = twitter.verifyCredentials()

  lazy val messageCreateRequestConsumer: ActorRef = RootResource.system.actorOf(Props[MessageCreateRequestConsumer])

  def posts(screenname : String) : List[Tweet] = {
    val timelineBuffer = scala.collection.mutable.ArrayBuffer.empty[Tweet]
    val timelineProviderConfiguration = timelineConfiguration
      .withInfo(List(screenname))
      .asInstanceOf[TwitterTimelineProviderConfiguration]
    val timelineProvider = new TwitterTimelineProvider(timelineProviderConfiguration)
    timelineProvider.prepare(timelineProviderConfiguration)
    timelineProvider.startStream()

    do {
      Uninterruptibles.sleepUninterruptibly(streamsConfiguration.getBatchFrequencyMs, TimeUnit.MILLISECONDS)
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
}

@RestResource(
  //  defaultRequestHeaders = Array("Accept: application/json", "Content-Type: application/json"),
  //  defaultResponseHeaders = Array("Content-Type: application/json"),
  htmldoc=new HtmlDoc(
    header=Array("Probot > Twitter"),
    links=Array("options: '?method=OPTIONS'"),
    footer=Array("ASF 2.0 License")
  ),
  path = "/twitter",
  title = "probot.Twitter",
  description = "probot.Twitter",
  converters=Array(classOf[Traversable],classOf[Queryable],classOf[Introspectable]),
  properties=Array(new Property(name = "REST_allowMethodParam", value = "*")),
  children = Array(
    classOf[probot.TopDomains],
    classOf[probot.TopHashtags],
    classOf[probot.TopMentions],
    classOf[probot.WebhookResource]
  )
)
class TwitterResource extends RemoteableServlet {
  import TwitterResource._

  @throws[ServletException]
  override def init(): Unit = {
    this.log("init")
  }

  @throws[Exception]
  override def getServiceMap() : util.Map[Class[_],Object] = {
    Map[Class[_],Object](
      classOf[Twitter] -> twitter,
      classOf[Account] -> twitter,
      classOf[AccountActivity] -> twitter,
      classOf[Favorites] -> twitter,
      classOf[Friends] -> twitter,
      classOf[Geo] -> twitter,
      classOf[Media] -> twitter,
      classOf[Statuses] -> twitter,
      classOf[Users] -> twitter,
      classOf[WelcomeMessageRules] -> twitter,
      classOf[WelcomeMessages] -> twitter
    ).asJava
  }
}
