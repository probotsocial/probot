package probot

import java.io.IOException

import org.apache.juneau.ObjectMap
import org.apache.juneau.rest.BasicRestServlet
import org.apache.juneau.rest.annotation.HtmlDoc
import org.apache.juneau.rest.annotation.Property
import org.apache.juneau.rest.annotation.RestMethod
import org.apache.juneau.rest.annotation.RestResource
import org.apache.juneau.rest.converters.Introspectable
import org.apache.juneau.rest.converters.Queryable
import org.apache.juneau.rest.converters.Traversable
import org.apache.juneau.rest.RestRequest
import org.apache.juneau.rest.RestResponse
import TwitterResource.twitter
import akka.actor.ActorRef
import akka.actor.Props
import javax.ws.rs.Consumes
import javax.ws.rs.Produces
import javax.ws.rs.ext.Provider
import org.apache.juneau.http.Accept
import org.apache.juneau.http.ContentType
import org.apache.juneau.http.annotation.Body
import org.apache.juneau.json.JsonParser
import org.apache.streams.twitter.api.MessageCreateRequest
import org.apache.streams.twitter.pojo.DirectMessageEvent
import org.apache.streams.twitter.pojo.MessageCreate
import org.apache.streams.twitter.pojo.MessageData
import org.apache.streams.twitter.pojo.Target;

@RestResource(
  debug = "true",
  defaultRequestHeaders = Array("Accept: application/json", "Content-Type: application/json"),
  htmldoc=new HtmlDoc(
    footer=Array("ASF 2.0 License"),
    header=Array("Probot > Twitter > DirectMessage"),
    navlinks=Array("options: '?method=OPTIONS'")
  ),
  path = "/directmessage",
  title = Array("probot.DirectMessage"),
  description = Array("probot.DirectMessage"),
  converters=Array(classOf[Traversable],classOf[Queryable],classOf[Introspectable]),
  properties=Array(new Property(name = "REST_allowMethodParam", value = "*"))
)
class DirectMessageResource extends BasicRestServlet {
  import ConfigurationResource.welcomeMessage
  import TwitterResource.user

  lazy val messageCreateRequestConsumer: ActorRef = RootResource.system.actorOf(Props[MessageCreateRequestConsumer])

  @RestMethod(name = "POST")
  @Consumes(Array("application/json"))
  @Produces(Array("application/json"))
  @throws[IOException]
  def doPost( req : RestRequest , res : RestResponse ): Unit = {

    val request: ObjectMap = JsonParser.DEFAULT.parse( req.getBody().asString(), classOf[ObjectMap])
    val ids: Array[String] = request.getStringArray("ids")

    for( id <- ids.map(_.split(':')(1)) ) {
      val messageData = new MessageData()
        .withText(welcomeMessage)
      val messageCreateRequest : MessageCreateRequest = new MessageCreateRequest()
        .withEvent(new DirectMessageEvent()
          .withMessageCreate(new MessageCreate()
            .withMessageData(messageData)
            .withTarget(new Target()
              .withRecipientId(id))
            .withSenderId(user.getIdStr)
          )
        )
      messageCreateRequestConsumer ! messageCreateRequest
    }

    val response: ObjectMap = new ObjectMap()
    response.append("ids", ids)
    res.setStatus(200)
    res.setOutput(response)

  }

}
