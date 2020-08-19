package social.probot.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import org.apache.streams.twitter.pojo.DirectMessageEvent
import org.apache.streams.twitter.pojo.WebhookEvents

import scala.collection.JavaConversions._

class WebhookEventsConsumer extends Actor with ActorLogging {

  override def preStart(): Unit = log.info("WebhookEventsConsumer actor started")
  override def postStop(): Unit = log.info("WebhookEventsConsumer actor stopped")

  lazy val directMessageEventConsumer: ActorRef = context.actorOf(Props[DirectMessageEventConsumer])
  lazy val directMessageEventPersister: ActorRef = context.actorOf(Props[DirectMessageEventPersister])

  def processDirectMessageEvent(event: DirectMessageEvent) = {
    directMessageEventPersister ! event
    directMessageEventConsumer ! event
  }

  override def receive: Receive = {
    case events : WebhookEvents =>
      events.getDirectMessageEvents.foreach(processDirectMessageEvent(_))
  }

}