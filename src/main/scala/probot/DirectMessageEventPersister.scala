package probot

import akka.actor.Actor
import akka.actor.ActorLogging
import org.apache.juneau.ObjectMap
import org.apache.streams.twitter.pojo.DirectMessageEvent
import org.slf4j.LoggerFactory
import probot.ProbotPersistance.probotPersistance

class DirectMessageEventPersister extends Actor with ActorLogging {

  private final val LOGGER = LoggerFactory.getLogger(classOf[OptInEventPersister])

  override def preStart(): Unit = log.info("DirectMessageEventPersister actor started")
  override def postStop(): Unit = log.info("DirectMessageEventPersister actor stopped")

  override def receive: Receive = {
    case directMessageEvent: DirectMessageEvent => {
      val directMessageEventMap = new ObjectMap()
        .append("id", directMessageEvent.getId)
        .append("json", directMessageEvent)
      probotPersistance.persistDirectMessageEvent(directMessageEventMap)
    }
    case _ => {
      LOGGER.warn("Unrecognized input to DirectMessageEventPersister")
    }
  }

}

