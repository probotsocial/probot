package probot

import java.io.IOException
import javax.servlet.ServletException
import javax.ws.rs.core.Response

import org.apache.juneau.ObjectMap
import org.apache.juneau.json.JsonParser
import org.apache.juneau.microservice.Resource
import org.apache.juneau.rest.annotation.{Body, Header, HtmlDoc, Query, RestMethod, RestResource}
import org.apache.juneau.rest.{RestRequest, RestResponse}
import org.apache.streams.config.{ComponentConfigurator, StreamsConfigurator}
import org.apache.streams.twitter.TwitterOAuthConfiguration
import org.apache.streams.twitter.api.{TwitterOAuthRequestInterceptor, Webhook, WelcomeMessageNewRequest, WelcomeMessageNewRequestWrapper, WelcomeMessageNewRuleRequest, WelcomeMessageNewRuleRequestWrapper, WelcomeMessageRulesListRequest, WelcomeMessagesListRequest}
import org.apache.streams.twitter.pojo.{MessageData, WebhookEvents, WelcomeMessage, WelcomeMessageRule}
import probot.TwitterResource.twitter

import scala.collection.JavaConversions._
import scala.util.Try

object WebhookResource {
	lazy val consumerSecret = new ComponentConfigurator(classOf[TwitterOAuthConfiguration]).detectConfiguration(StreamsConfigurator.getConfig().getConfig("twitter").getConfig("oauth")).getConsumerSecret;
	lazy val webhookUrl = StreamsConfigurator.getConfig.getString("webhook")
	lazy val welcomeMessage = StreamsConfigurator.getConfig.getString("welcome_message");

	lazy val webhook : Webhook = {
		val webhooks = twitter.getWebhooks
		webhooks.foreach((webhook : Webhook) => println(webhook))
		// TODO: clean up
		// need deletes to be working
		val activeWebhook : Option[Webhook] = Try(webhooks.filter(_.getUrl.eq(webhookUrl)).get(0)).toOption
		// update to match conf
		if( activeWebhook.isEmpty ) {
			twitter.registerWebhook(webhookUrl)
		} else {
			activeWebhook.get
		}
	}
	lazy val message : WelcomeMessage = {
		val messages = twitter.listWelcomeMessages(new WelcomeMessagesListRequest()).getWelcomeMessages
		messages.foreach((message : WelcomeMessage) => println(message))
		var activeMessage: Option[WelcomeMessage] = Try(messages.filter(_.getMessageData.getText.eq(welcomeMessage)).get(0)).toOption
		// update to match conf
		if( activeMessage.isEmpty ) {
			twitter.newWelcomeMessage(new WelcomeMessageNewRequest()
				.withWelcomeMessage(new WelcomeMessageNewRequestWrapper()
					.withMessageData(new MessageData()
						.withText(welcomeMessage)
					)
				)
			).getWelcomeMessage
		} else {
			activeMessage.get
		}
	}
	lazy val rule : WelcomeMessageRule = {
		val rules = twitter.listWelcomeMessageRules(new WelcomeMessageRulesListRequest()).getWelcomeMessageRules
		// compare to conf
		rules.foreach((rule : WelcomeMessageRule) => println(rule))
		// update to match conf
		var activeRule: Option[WelcomeMessageRule] = Try(rules.get(0)).toOption
		if( activeRule.isEmpty ) {
			twitter.newWelcomeMessageRule(message.getId.toLong)
		} else {
			activeRule.get
		}
	}
	lazy val subscribed : Boolean = {
		val activeSubscription = twitter.getWebhookSubscription(webhook.getId)
		// update to match conf
		if( !activeSubscription ) {
			twitter.registerWebhookSubscriptions(webhook.getId)
		} else {
			activeSubscription
		}
	}
}

@RestResource(
	defaultRequestHeaders = Array("Accept: application/json", "Content-Type: application/json"),
	//  defaultResponseHeaders = Array("Content-Type: application/json"),
	htmldoc=new HtmlDoc(
		aside=""
			+ "<div style='max-width:400px;min-width:200px'>"
			+ "Probot.Webhook"
			+ "</div>",
		nav="probot > Twitter > Webhook",
		title="Probot.Webhook",
		description="Probot.Webhook",
		header="Probot.Webhook",
		footer="ASF 2.0 License"
	),
	path = "/webhook",
	title = "probot.Webhook",
	description = "probot.Webhook"
)
class WebhookResource extends Resource {

	import TwitterResource._
	import WebhookResource._

	import scala.concurrent.ExecutionContext.Implicits.global

	@throws[ServletException]
	override def init(): Unit = {
		this.log("init")
	}

	def computeCRC(crc_token: String): String = {
		"sha256="+TwitterOAuthRequestInterceptor.computeSignature(crc_token, consumerSecret)
	}

	@RestMethod(name = "GET")
	@throws[IOException]
	def get(req: RestRequest,
					res: RestResponse,
					@Query("crc_token") crc_token: String) = {
		val hash: String = computeCRC(crc_token)
		assert(hash.startsWith("sha256"))
		val response: ObjectMap = new ObjectMap().append("response_token", hash)
		res.setOutput(response)
		res.setStatus(Response.Status.OK.getStatusCode)
	}

	@RestMethod(name = "POST")
	@throws[IOException]
	def post(req: RestRequest,
					 res: RestResponse,
					 @Header("X-Twitter-Webhooks-Signature") signature : String) = {
		val hash: String = computeCRC(req.getBody.asString())
		assert(signature.startsWith("sha256"))
		if(!signature.equals(hash)) {
			res.setStatus(Response.Status.BAD_REQUEST.getStatusCode)
		} else {
			val webhookEvents = Try(JsonParser.DEFAULT.parse(req.getBody.asString(), classOf[WebhookEvents])).toOption
			webhookEvents match {
				case Some(events : WebhookEvents) => {
					res.setStatus(Response.Status.OK.getStatusCode)
					res.setOutput(Response.Status.OK)
					global.execute(new WebhookEventsConsumer(events))
				}
				case None => {
					res.setOutput(Response.Status.BAD_REQUEST)
					res.setStatus(Response.Status.BAD_REQUEST.getStatusCode)
				}
			}
		}
	}
}
