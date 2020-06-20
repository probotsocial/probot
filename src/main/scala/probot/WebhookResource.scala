package probot

import java.io.IOException
import java.lang

import javax.ws.rs.core.Response
import akka.actor.{ActorRef, Props}
import org.apache.http.client.utils.URIBuilder
import org.apache.juneau.ObjectMap
import org.apache.juneau.http.annotation.Header
import org.apache.juneau.http.annotation.Query
import org.apache.juneau.json.JsonParser
import org.apache.juneau.rest.BasicRestServlet
import org.apache.juneau.rest.RestContextBuilder
import org.apache.juneau.rest.annotation.{HtmlDoc, RestMethod, RestResource}
import org.apache.juneau.rest.{RestException, RestRequest, RestResponse}
import org.apache.streams.config.{ComponentConfigurator, StreamsConfigurator}
import org.apache.streams.twitter.api.{TwitterOAuthRequestInterceptor, TwitterSecurity, Webhook, WelcomeMessageNewRequest, WelcomeMessageNewRequestWrapper, WelcomeMessageNewRuleRequest, WelcomeMessageNewRuleRequestWrapper, WelcomeMessageRulesListRequest, WelcomeMessagesListRequest}
import org.apache.streams.twitter.config.TwitterOAuthConfiguration
import org.apache.streams.twitter.pojo.{MessageData, WebhookEvents, WelcomeMessage, WelcomeMessageRule}

import scala.collection.JavaConversions._
import scala.util.Try

object WebhookResource {

	val serverConfig = StreamsConfigurator.getConfig().getConfig("server")
	val url : String = new URIBuilder(RootResource.asUri(serverConfig)).toString

	val consumerSecret = new ComponentConfigurator(classOf[TwitterOAuthConfiguration]).detectConfiguration(StreamsConfigurator.getConfig().getConfig("twitter").getConfig("oauth")).getConsumerSecret;
	val welcomeMessage = StreamsConfigurator.getConfig.getString("welcome_message");

	lazy val webhook : Webhook = initWebhook(ConfigurationResource.twitter.getEnvironment)
	lazy val message : WelcomeMessage = initWelcomeMessage
	lazy val rule : WelcomeMessageRule = initWelcomeMessageRule
	lazy val subscribed : Boolean = initSubscribed(ConfigurationResource.twitter.getEnvironment)

	lazy val webhookEventsConsumer: ActorRef = RootResource.system.actorOf(Props[WebhookEventsConsumer])

	def initWebhook(environment : String) = {
		val webhooks = TwitterResource.twitter.getWebhooks(environment)
		webhooks.foreach((webhook : Webhook) => println(webhook.getUrl))
		// need deletes to be working
		val activeWebhook : Option[Webhook] = Try(webhooks.filter(_.getUrl == url).get(0)).toOption
		// update to match conf
		if( webhooks.size > 0 &&
				activeWebhook.isDefined &&
				!activeWebhook.get.getUrl.equals(url)) {
			webhooks.foreach((webhook : Webhook) => TwitterResource.twitter.deleteWebhook(environment, webhook.getId.toLong))
		}
		if( activeWebhook.isEmpty ) {
			TwitterResource.twitter.registerWebhook(environment, url)
		} else {
			activeWebhook.get
		}
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
	def initWelcomeMessageRule = {
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
						.withWelcomeMessageId(message.getId)
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
		  .append("subscribed", subscribed)
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
