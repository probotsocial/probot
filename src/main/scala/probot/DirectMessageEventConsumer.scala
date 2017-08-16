package probot

import java.net.URL

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import org.apache.streams.twitter.api.MessageCreateRequest
import org.apache.streams.twitter.pojo.{DirectMessageEvent, MessageCreate, MessageData, Target}

object DirectMessageEventConsumer {

  case class HelloEvent() extends DirectMessageEvent
  object HelloEvent {
    def unapply(event: DirectMessageEvent) = {
      if( event.getMessageCreate.getMessageData.getText.equals("hello") )
        Some(new HelloEvent())
      else None
    }
  }

  case class PingEvent() extends DirectMessageEvent
  object PingEvent {
    def unapply(event: DirectMessageEvent) = {
      if( event.getMessageCreate.getMessageData.getText.equals("ping") )
        Some(new PingEvent())
      else None
    }
  }

  case class EchoEvent(val text : String) extends DirectMessageEvent
  object EchoEvent {
    val echoKeywords = List("foo", "bar")
    def unapply(event: DirectMessageEvent) = {
      if( echoKeywords.contains(event.getMessageCreate.getMessageData.getText)) {
        Some(new EchoEvent(event.getMessageCreate.getMessageData.getText))
      }
      else None
    }
  }

  case class RankEvent(val field: String) extends DirectMessageEvent
  object RankEvent {
    val superlativeKeywords = List("list", "top", "rank", "best")
    val domainKeywords = List("domain", "site", "web")
    val mentionKeywords = List("mention", "friend")
    def unapply(event: DirectMessageEvent): Option[RankEvent] = {
      if( superlativeKeywords.exists(event.getMessageCreate.getMessageData.getText.contains)) {
        if( event.getMessageCreate.getMessageData.getText.contains("hashtag") ) {
          return Some(new RankEvent("hashtags"))
        } else if( domainKeywords.exists(event.getMessageCreate.getMessageData.getText.contains )) {
          return Some(new RankEvent("domains"))
        } else if ( mentionKeywords.exists(event.getMessageCreate.getMessageData.getText.contains )) {
          return Some(new RankEvent("mentions"))
        }
      }
      None
    }
  }

}

class DirectMessageEventConsumer extends Actor with ActorLogging {

  import DirectMessageEventConsumer._
  import TwitterResource._

  override def preStart(): Unit = log.info("DirectMessageEventConsumer actor started")
  override def postStop(): Unit = log.info("DirectMessageEventConsumer actor stopped")

  lazy val messageCreateRequestConsumer: ActorRef = context.actorOf(Props[MessageCreateRequestConsumer])

  def processMessageData(messageData: MessageData, sendTo: String, sendAs: String = TwitterResource.user.getIdStr) = {
    val messageCreateRequest = new MessageCreateRequest()
      .withEvent(new DirectMessageEvent()
        .withMessageCreate(new MessageCreate()
          .withMessageData(messageData)
          .withTarget(new Target()
            .withRecipientId(sendTo))
          .withSenderId(sendAs)
        )
      )
    messageCreateRequestConsumer ! messageCreateRequest
  }


  override def receive: Receive = {
    case event: DirectMessageEvent => {
      val senderId = event.getMessageCreate.getSenderId
      val recipientId = event.getMessageCreate.getTarget.getRecipientId
      if (recipientId.equals(user.getIdStr)) {
        processMessageData(response(classify(event)), senderId, recipientId)
      }
    }
  }

  def classify(event : DirectMessageEvent) : DirectMessageEvent =
    event match {
      case HelloEvent(hello) => hello
      case PingEvent(ping) => ping
      case EchoEvent(echo) => echo
      case RankEvent(rank) => rank
      case _ : DirectMessageEvent => event
    }

  def response(event : DirectMessageEvent) =
    event match {
      case hello : HelloEvent => respondTo(hello)
      case ping : PingEvent => respondTo(ping)
      case echo : EchoEvent => respondTo(echo)
      case rank : RankEvent => respondTo(rank)
      case _ : DirectMessageEvent => respondSorry
    }

  def respondTo(event : HelloEvent) : MessageData = {
    response(text = "hello!")
  }

  def respondTo(event: PingEvent) : MessageData = {
    response(text = pingResponse())
  }

  def respondTo(event: EchoEvent) : MessageData = {
    response(text = event.text)
  }

  def listURL(event: RankEvent): URL = {
    event.field match {
      case "domains" => new URL(TopDomains.url.toString)
      case "hashtags" => new URL(TopHashtags.url.toString)
      case "mentions" => new URL(TopMentions.url.toString)
    }
  }

  def respondTo(event: RankEvent) : MessageData = {
    response("Here's a list of my top "+event.field, Some(listURL(event)))
  }

  def respondSorry() : MessageData = {
    response("sorry")
  }

  def pingResponse() : String = {
    "pong"
  }

  def response(text: String, url : Option[URL] = None ): MessageData = {
    new MessageData()
      .withText(text + {if (url.isDefined) {" " + url.toString}})
  }

}

