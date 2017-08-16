package probot

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import org.apache.streams.twitter.pojo.{DirectMessageEvent, WebhookEvents}

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global

class WebhookEventsConsumer extends Actor with ActorLogging {

  override def preStart(): Unit = log.info("WebhookEventsConsumer actor started")
  override def postStop(): Unit = log.info("WebhookEventsConsumer actor stopped")

  lazy val directMessageEventConsumer: ActorRef = context.actorOf(Props[DirectMessageEventConsumer])

  def processDirectMessageEvent(event: DirectMessageEvent) = {
    directMessageEventConsumer ! event
  }

  override def receive: Receive = {
    case events : WebhookEvents =>
      events.getDirectMessageEvents.foreach(processDirectMessageEvent(_))
  }

}