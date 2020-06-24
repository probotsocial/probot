package probot

import java.net.URL

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import org.apache.commons.lang3.StringUtils
import org.apache.juneau.ObjectMap
import org.apache.juneau.json.JsonParser
import org.apache.juneau.json.JsonSerializer
import org.apache.juneau.rest.client.RestClient
import org.apache.streams.components.http.HttpConfiguration
import org.apache.streams.config.ComponentConfigurator
import org.apache.streams.config.StreamsConfigurator
import org.apache.streams.twitter.api.MessageCreateRequest
import org.apache.streams.twitter.pojo._

class DirectMessageEventPersister extends Actor with ActorLogging {

  override def preStart(): Unit = log.info("DirectMessageEventPersister actor started")
  override def postStop(): Unit = log.info("DirectMessageEventPersister actor stopped")

  val config = StreamsConfigurator.getConfig.getConfig("DirectMessageEventPersister")
  val client = new ComponentConfigurator[HttpConfiguration](classOf[HttpConfiguration]).detectConfiguration(config)
  val url = client.getProtocol+"://"+client.getHostname+":"+client.getPort
  val restClientBuilder = RestClient.create()
    .parser(JsonParser.DEFAULT)
    .rootUrl(url)
    .serializer(JsonSerializer.DEFAULT)
    .trimEmptyCollections(true)
    .trimEmptyMaps(true);
  val withAuth = {
    if( StringUtils.isNotBlank(client.getUsername) && StringUtils.isNotBlank(client.getPassword))
      restClientBuilder.basicAuth(client.getHostname, client.getPort.intValue(), client.getUsername, client.getPassword)
    else restClientBuilder
  }
  val restClient = withAuth.build()
  val directMessageEventPersistance = restClient.getRemoteResource(classOf[DirectMessageEventPersistance])

  override def receive: Receive = {
    case event: DirectMessageEvent => {
      val insertMap = new ObjectMap()
        .append("id", event.getId)
        .append("json", event)
      directMessageEventPersistance.persistDirectMessageEvent(insertMap)
    }
  }

}

