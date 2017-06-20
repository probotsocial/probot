package probot


import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.servlet.ServletException

import com.fasterxml.jackson.core.JsonProcessingException
import com.google.common.util.concurrent.Uninterruptibles
import org.apache.http.HttpResponse
import org.apache.juneau.ObjectMap
import org.apache.juneau.microservice.{Resource, ResourceGroup}
import org.apache.juneau.rest.{RestRequest, RestResponse}
import org.apache.juneau.rest.annotation.{HtmlDoc, RestMethod, RestResource}
import org.apache.streams.config.{ComponentConfigurator, StreamsConfiguration, StreamsConfigurator}
import org.apache.streams.twitter.{TwitterConfiguration, TwitterTimelineProviderConfiguration}
import org.apache.streams.twitter.api.{AccountSettings, Twitter, TwitterSecurity}
import org.apache.streams.twitter.pojo.{Tweet, User}
import org.apache.streams.twitter.provider.TwitterTimelineProvider

import scala.collection.JavaConversions._

object TwitterResource {
  lazy val streamsConfiguration: StreamsConfiguration = StreamsConfigurator.detectConfiguration()
  lazy val twitterConfiguration: TwitterConfiguration = new ComponentConfigurator(classOf[TwitterConfiguration]).detectConfiguration(StreamsConfigurator.getConfig().getConfig("twitter"));
  lazy val twitter: org.apache.streams.twitter.api.Twitter = Twitter.getInstance(twitterConfiguration)
  lazy val twitterSecurity: org.apache.streams.twitter.api.TwitterSecurity = new TwitterSecurity

  lazy val timelineConfiguration: TwitterTimelineProviderConfiguration = new ComponentConfigurator(classOf[TwitterTimelineProviderConfiguration]).detectConfiguration(StreamsConfigurator.getConfig().getConfig("twitter"));

  lazy val accountSettings: AccountSettings = twitter.settings()
  lazy val timeline: List[Tweet] = posts(accountSettings.getScreenName)
  lazy val user: User = twitter.verifyCredentials()

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
    aside=""
      + "<div style='max-width:400px;min-width:200px'>"
      + "Probot.Twitter"
      + "</div>",
    nav="probot > Twitter",
    title="Probot.Twitter",
    description="Probot.Twitter",
    header="Probot.Twitter",
    footer="ASF 2.0 License"
  ),
  path = "/twitter",
  title = "probot.Twitter",
  description = "probot.Twitter",
  children = Array(
    classOf[probot.TopDomains],
    classOf[probot.TopHashtags],
    classOf[probot.TopMentions],
    classOf[probot.WebhookResource]
  )
)
class TwitterResource extends Resource {
  import TwitterResource._

  @throws[ServletException]
  override def init(): Unit = {
    this.log("init")
  }

  @RestMethod(name = "GET")
  @throws[IOException]
  def get(req: RestRequest,
          res: RestResponse) = {

    val objectMap = new ObjectMap()
      .append("streamsConfiguration", streamsConfiguration)
      .append("twitterConfiguration", twitterConfiguration)
      .append("timelineConfiguration", timelineConfiguration)
      .append("user", user)
      .append("timeline", timeline.size)

    res.setOutput(objectMap)
    res.setStatus(200)

  }
}
