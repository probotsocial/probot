package social.probot.microservice

import java.io.IOException

import akka.actor.ActorRef
import akka.actor.Props
import javax.ws.rs.core.Response
import org.apache.juneau.ObjectMap
import org.apache.juneau.http.annotation.Header
import org.apache.juneau.http.annotation.Query
import org.apache.juneau.json.JsonParser
import org.apache.juneau.rest.BasicRestServlet
import org.apache.juneau.rest.RestRequest
import org.apache.juneau.rest.RestResponse
import org.apache.juneau.rest.annotation.HtmlDoc
import org.apache.juneau.rest.annotation.RestMethod
import org.apache.juneau.rest.annotation.RestResource
import org.apache.streams.config.ComponentConfigurator
import org.apache.streams.config.StreamsConfigurator
import org.apache.streams.twitter.api.TwitterSecurity
import org.apache.streams.twitter.config.TwitterOAuthConfiguration
import org.apache.streams.twitter.pojo.WebhookEvents
import social.probot.actors.WebhookEventsConsumer

import scala.util.Try

object WebhookResource {

	val consumerSecret = new ComponentConfigurator(classOf[TwitterOAuthConfiguration]).detectConfiguration(StreamsConfigurator.getConfig().getConfig("twitter").getConfig("oauth")).getConsumerSecret;

	lazy val webhookEventsConsumer: ActorRef = RootResource.system.actorOf(Props[WebhookEventsConsumer])

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

 	def computeCRC(crc_token: String): String = {
		"sha256="+TwitterResource.twitterSecurity.computeAndEncodeSignature(crc_token, consumerSecret, TwitterSecurity.webhook_signature_method)
	}

	@RestMethod(name = "GET")
	@throws[IOException]
	def get(req: RestRequest,
					res: RestResponse,
					@Header("X-Twitter-Webhooks-Signature") signature : String,
					@Query("crc_token") crc_token: String) = {
		val response = new ObjectMap()
		val hash: String = computeCRC(crc_token)
		assert(hash.startsWith("sha256"))
		response.append("response_token", hash)
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
