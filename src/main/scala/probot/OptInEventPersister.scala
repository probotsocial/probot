package probot

import akka.actor.Actor
import akka.actor.ActorLogging
import org.apache.juneau.ObjectMap
import org.apache.streams.twitter.pojo.DirectMessageEvent
import org.slf4j.LoggerFactory
import probot.DirectMessageEventConsumer.EmailAddressEvent
import probot.DirectMessageEventConsumer.PhoneNumberEvent
import probot.ProbotPersistance.probotPersistance

class OptInEventPersister extends Actor with ActorLogging {

  private final val LOGGER = LoggerFactory.getLogger(classOf[OptInEventPersister])

  override def preStart(): Unit = log.info("OptInEventPersister actor started")
  override def postStop(): Unit = log.info("OptInEventPersister actor stopped")

  override def receive: Receive = {
    case email : EmailAddressEvent => {
      persistEvent(email.asInstanceOf[DirectMessageEvent], emailAddress = email.emailAddress)
    }
    case phone : PhoneNumberEvent => {
      persistEvent(phone.asInstanceOf[DirectMessageEvent], phoneNumber = phone.phoneNumber)
    }
    case _ => {
      LOGGER.warn("Unrecognized input to OptInEventPersister")
    }
  }

  def persistEvent(optInEvent : DirectMessageEvent, emailAddress : String = "", phoneNumber : String = "") = {
    val optInEventMap = new ObjectMap()
      .append("id", optInEvent.getId)
      .append("timestamp", optInEvent.getCreatedTimestamp)
      .appendIf(emailAddress.nonEmpty, "emailAddress", emailAddress)
      .appendIf(phoneNumber.nonEmpty, "phoneNumber", phoneNumber)
    probotPersistance.persistOptIn(optInEventMap)
  }

}

