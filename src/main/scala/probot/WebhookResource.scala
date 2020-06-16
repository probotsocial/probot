package probot

import java.io.IOException

import javax.ws.rs.core.Response
import akka.actor.{ActorRef, Props}
import org.apache.http.client.utils.URIBuilder
import org.apache.juneau.ObjectMap
import org.apache.juneau.http.annotation.Header
import org.apache.juneau.http.annotation.Query
import org.apache.juneau.json.JsonParser
import org.apache.juneau.rest.BasicRestServlet
import org.apache.juneau.rest.annotation.{HtmlDoc, RestMethod, RestResource}
import org.apache.juneau.rest.{RestException, RestRequest, RestResponse}
import org.apache.streams.config.{ComponentConfigurator, StreamsConfigurator}
import org.apache.streams.twitter.api.{TwitterOAuthRequestInterceptor, TwitterSecurity, Webhook, WelcomeMessageNewRequest, WelcomeMessageNewRequestWrapper, WelcomeMessageNewRuleRequest, WelcomeMessageNewRuleRequestWrapper, WelcomeMessageRulesListRequest, WelcomeMessagesListRequest}
import org.apache.streams.twitter.config.TwitterOAuthConfiguration
import org.apache.streams.twitter.pojo.{MessageData, WebhookEvents, WelcomeMessage, WelcomeMessageRule}
import probot.TwitterResource.twitter

import scala.collection.JavaConversions._
import scala.util.Try

object WebhookResource {

	val url : String = new URIBuilder(RootResource.asUri(StreamsConfigurator.getConfig().getConfig("server")))
		.setPath("/twitter/webhook").toString

	val consumerSecret = new ComponentConfigurator(classOf[TwitterOAuthConfiguration]).detectConfiguration(StreamsConfigurator.getConfig().getConfig("twitter").getConfig("oauth")).getConsumerSecret;
	val welcomeMessage = StreamsConfigurator.getConfig.getString("welcome_message");

	lazy val webhook : Webhook = initWebhook
	lazy val message : WelcomeMessage = initWelcomeMessage
	lazy val rule : WelcomeMessageRule = initWelcomeMessageRule
	var subscribed : Boolean = false

	lazy val webhookEventsConsumer: ActorRef = RootResource.system.actorOf(Props[WebhookEventsConsumer])

	def initWebhook = {
		val webhooks = twitter.getWebhooks
		webhooks.foreach((webhook : Webhook) => println(webhook))
		// TODO: clean up
		// need deletes to be working
		val activeWebhook : Option[Webhook] = Try(webhooks.filter(_.getUrl.eq(url)).get(0)).toOption
		// update to match conf
		if( activeWebhook.isEmpty ) {
			if( webhooks.size > 0 ) {
				webhooks.foreach((webhook : Webhook) => twitter.deleteWebhook(webhook.getId.toLong))
			}
			twitter.registerWebhook(url)
		} else {
			activeWebhook.get
		}
	}
	def initWelcomeMessage = {
		val messages = twitter.listWelcomeMessages(new WelcomeMessagesListRequest()).getWelcomeMessages
		var activeMessage: Option[WelcomeMessage] = Try(messages.filter(_.getMessageData.getText.eq(welcomeMessage)).get(0)).toOption
		// update to match conf
		if( activeMessage.isEmpty ) {
			if( messages.size > 0 ) {
				messages.foreach((message : WelcomeMessage) => twitter.destroyWelcomeMessage(message.getId.toLong))
			}
			def newWelcomeMessage = {
				new WelcomeMessageNewRequest()
					.withWelcomeMessage(new WelcomeMessageNewRequestWrapper()
						.withMessageData(new MessageData()
							.withText(welcomeMessage)
						)
					)
			}
			twitter.newWelcomeMessage(newWelcomeMessage).getWelcomeMessage
		} else {
			activeMessage.get
		}
	}
	def initWelcomeMessageRule = {
		val rules = twitter.listWelcomeMessageRules(new WelcomeMessageRulesListRequest()).getWelcomeMessageRules
		// compare to conf
		rules.foreach((rule : WelcomeMessageRule) => println(rule))
		// update to match conf
		var activeRule: Option[WelcomeMessageRule] = Try(rules.get(0)).toOption
		if( activeRule.isEmpty ) {
			if( rules.size > 0 ) {
				rules.foreach((rule : WelcomeMessageRule) => twitter.destroyWelcomeMessageRule(rule.getId.toLong))
			}
			def newWelcomeMessageRule = {
				new WelcomeMessageNewRuleRequest()
					.withWelcomeMessageRule(new WelcomeMessageNewRuleRequestWrapper()
						.withWelcomeMessageId(message.getId)
					)
			}
			twitter.newWelcomeMessageRule(newWelcomeMessageRule)
		} else {
			activeRule.get
		}
	}
	def initSubscribed : Boolean = {
		val activeSubscription = twitter.getWebhookSubscription(webhook.getId.toLong)
		if( !activeSubscription ) {
			val nowSubscribed = twitter.registerWebhookSubscriptions(webhook.getId.toLong)
			nowSubscribed
		} else {
			activeSubscription
		}
	}
}

@RestResource(
	defaultRequestHeaders = Array("Accept: application/json", "Content-Type: application/json"),
	//  defaultResponseHeaders = Array("Content-Type: application/json"),
	htmldoc=new HtmlDoc(
		header=Array("Probot > Webhook"),
		footer=Array("ASF 2.0 License")
	),
	path = "/webhook",
	title = Array("probot.Webhook"),
	description = Array("probot.Webhook")
)
class WebhookResource extends BasicRestServlet {

	import WebhookResource._

	import scala.concurrent.ExecutionContext.Implicits.global

	def computeCRC(crc_token: String): String = {
		"sha256="+TwitterResource.twitterSecurity.computeAndEncodeSignature(crc_token, consumerSecret, TwitterSecurity.webhook_signature_method)
	}

	def doCrc(crc_token: String): ObjectMap = {
		var objectMap = new ObjectMap()
		val hash: String = computeCRC(crc_token)
		assert(hash.startsWith("sha256"))
		objectMap.append("response_token", hash)
	}

	def info(): ObjectMap = {
		var info = new ObjectMap()
			.append("url", url)
			.append("welcomeMessage", welcomeMessage)
			.append("webhook", webhook)
			.append("message", message)
			.append("rule", rule)
		if( !subscribed ) subscribed = initSubscribed
		info = info.append("subscribed", subscribed)
		info
	}

	@RestMethod(name = "GET")
	@throws[IOException]
	def get(req: RestRequest,
					res: RestResponse,
					@Header("X-Twitter-Webhooks-Signature") signature : String,
					@Query("crc_token") crc_token: String) = {
		val response: ObjectMap = {
			if( crc_token != null && !crc_token.isEmpty ) {
				doCrc(crc_token)
			} else {
				info()
			}
		}
		res.setOutput(response)
		res.setStatus(Response.Status.OK.getStatusCode)
	}

	@RestMethod(name = "POST")
	@throws[IOException]
	def post(req: RestRequest,
					 res: RestResponse) = {
		val hash: String = computeCRC(req.getBody.asString())
		val webhookEvents = Try(JsonParser.DEFAULT.parse(req.getBody.asString(), classOf[WebhookEvents])).toOption
		webhookEvents match {
			case Some(events : WebhookEvents) => {
				res.setStatus(Response.Status.OK.getStatusCode)
				res.setOutput(Response.Status.OK)
				webhookEventsConsumer ! events
			}
			case None => {
				res.setOutput(Response.Status.BAD_REQUEST)
				res.setStatus(Response.Status.BAD_REQUEST.getStatusCode)
			}
		}
	}

}
