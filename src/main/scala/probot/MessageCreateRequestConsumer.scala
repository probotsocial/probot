package probot

import akka.actor.{Actor, ActorLogging}
import org.apache.streams.twitter.api.MessageCreateRequest

class MessageCreateRequestConsumer extends Actor with ActorLogging {

  override def preStart(): Unit = log.info("MessageCreateRequestConsumer actor started")
  override def postStop(): Unit = log.info("MessageCreateRequestConsumer actor stopped")

  override def receive: Receive = {
    case event: MessageCreateRequest => {
      TwitterResource.twitter.newEvent(event)
    }
  }
}