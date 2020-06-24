package probot

import java.io.IOException

import org.apache.http.client.utils.URIBuilder
import org.apache.juneau.ObjectMap
import org.apache.juneau.rest.BasicRestServlet
import org.apache.juneau.rest.RestRequest
import org.apache.juneau.rest.RestResponse
import org.apache.juneau.rest.annotation.HtmlDoc
import org.apache.juneau.rest.annotation.RestResource
import org.apache.streams.config.StreamsConfigurator
import org.apache.streams.twitter.api.Webhook
import org.apache.streams.twitter.api.WelcomeMessageNewRequest
import org.apache.streams.twitter.api.WelcomeMessageNewRequestWrapper
import org.apache.streams.twitter.api.WelcomeMessageNewRuleRequest
import org.apache.streams.twitter.api.WelcomeMessageNewRuleRequestWrapper
import org.apache.streams.twitter.api.WelcomeMessageRulesListRequest
import org.apache.streams.twitter.api.WelcomeMessagesListRequest
import org.apache.streams.twitter.pojo.MessageData
import org.apache.streams.twitter.pojo.WelcomeMessage
import org.apache.streams.twitter.pojo.WelcomeMessageRule
import org.apache.juneau.rest.annotation.HookEvent.POST_INIT
import org.apache.juneau.rest.annotation.RestHook
import org.apache.juneau.rest.annotation.RestMethod
import org.apache.streams.config.ComponentConfigurator

import scala.collection.JavaConversions._
import scala.util.Try

object AccountActivityResource {

  import ConfigurationResource.url
  import ConfigurationResource.welcomeMessage

  import TwitterResource.twitter

  def initWebhook(environment : String) : Webhook = {
    val existingWebhooks = twitter.getWebhooks(environment)
    val existingConfiguredWebhook : Option[Webhook] = Try(existingWebhooks.filter(_.getUrl == url).get(0)).toOption
    // update to match conf
    if( existingWebhooks.size == 0 ) {
      return twitter.registerWebhook(environment, url)
    }
    if( existingConfiguredWebhook.isDefined && existingConfiguredWebhook.get.getValid() == true) {
      return existingConfiguredWebhook.get
    }
    if( existingConfiguredWebhook.isDefined ) {
      twitter.reenableWebhook(environment, existingConfiguredWebhook.get.getId.toLong)
    }
    if( existingConfiguredWebhook.isEmpty ) {
      existingWebhooks.foreach((webhook: Webhook) => Try(twitter.deleteWebhook(environment, webhook.getId.toLong)))
      twitter.registerWebhook(environment, url)
    }
    val updatedWebhooks = twitter.getWebhooks(environment)
    val updatedConfiguredWebhook = updatedWebhooks.filter(_.getUrl == url).get(0)
    updatedConfiguredWebhook
  }

  def initWelcomeMessage = {
    val messages = TwitterResource.twitter.listWelcomeMessages(new WelcomeMessagesListRequest()).getWelcomeMessages
    var activeMessage: Option[WelcomeMessage] = Try(messages.filter(_.getMessageData.getText.eq(welcomeMessage)).get(0)).toOption
    // update to match conf
    if( activeMessage.isEmpty ) {
      if( messages.size > 0 ) {
        messages.foreach((message : WelcomeMessage) => TwitterResource.twitter.destroyWelcomeMessage(message.getId.toLong))
      }
      def newWelcomeMessage = {
        new WelcomeMessageNewRequest()
          .withWelcomeMessage(new WelcomeMessageNewRequestWrapper()
            .withMessageData(new MessageData()
              .withText(welcomeMessage)
            )
          )
      }
      TwitterResource.twitter.newWelcomeMessage(newWelcomeMessage).getWelcomeMessage
    } else {
      activeMessage.get
    }
  }
  def initWelcomeMessageRule(welcomeMessage: WelcomeMessage) = {
    val rules = TwitterResource.twitter.listWelcomeMessageRules(new WelcomeMessageRulesListRequest()).getWelcomeMessageRules
    // compare to conf
    rules.foreach((rule : WelcomeMessageRule) => println(rule))
    // update to match conf
    var activeRule: Option[WelcomeMessageRule] = Try(rules.get(0)).toOption
    if( activeRule.isEmpty ) {
      if( rules.size > 0 ) {
        rules.foreach((rule : WelcomeMessageRule) => TwitterResource.twitter.destroyWelcomeMessageRule(rule.getId.toLong))
      }
      def newWelcomeMessageRule = {
        new WelcomeMessageNewRuleRequest()
          .withWelcomeMessageRule(new WelcomeMessageNewRuleRequestWrapper()
            .withWelcomeMessageId(welcomeMessage.getId)
          )
      }
      TwitterResource.twitter.newWelcomeMessageRule(newWelcomeMessageRule)
    } else {
      activeRule.get
    }
  }
  def initSubscribed(environment : String) : Boolean = {
    var subscribed: Boolean = TwitterResource.twitter.getSubscriptions(environment)
    if( !subscribed ) {
      subscribed = TwitterResource.twitter.newSubscription(environment)
    }
    subscribed
  }
}

@RestResource(
  //defaultRequestHeaders = Array("Accept: application/json", "Content-Type: application/json"),
  htmldoc=new HtmlDoc(
    header=Array("Probot > Twitter > Activity"),
    footer=Array("ASF 2.0 License")
  ),
  path = "/activity",
  title = Array("probot.twitter.Activity"),
  description = Array("probot.twitter.Activity")
)
class AccountActivityResource extends BasicRestServlet {

  import AccountActivityResource._

  @RestMethod(name = "GET")
  @throws[IOException]
  def get(req: RestRequest, res: RestResponse) = {
    val objectMap = new ObjectMap().
      append("subscribed", connectionStatus())
    res.setOutput(objectMap)
    res.setStatus(200)
  }

  def connectionStatus(): Boolean = {
    val webhook : Webhook = initWebhook(ConfigurationResource.twitter.getEnvironment)
    val welcomeMessage : WelcomeMessage = initWelcomeMessage
    val welcomeMessageRule : WelcomeMessageRule = initWelcomeMessageRule(welcomeMessage)
    val subscribed : Boolean = initSubscribed(ConfigurationResource.twitter.getEnvironment)
    subscribed
  }

}
