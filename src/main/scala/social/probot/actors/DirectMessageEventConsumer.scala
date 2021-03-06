package social.probot.actors

import java.net.URL

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import org.apache.commons.validator.EmailValidator
import org.apache.streams.twitter.api.MessageCreateRequest
import org.apache.streams.twitter.pojo._
import social.probot.actors.DirectMessageEventConsumer.RankEntity.RankEntity
import social.probot.microservice.TwitterResource

import scala.util.Try

object DirectMessageEventConsumer {

  case class HelloEvent() extends DirectMessageEvent
  object HelloEvent {
    def unapply(event: DirectMessageEvent) = {
      if( event.getMessageCreate.getMessageData.getText.toLowerCase.equals("hello") )
        Some(new HelloEvent())
      else None
    }
  }

  case class PingEvent() extends DirectMessageEvent
  object PingEvent {
    def unapply(event: DirectMessageEvent) = {
      if( event.getMessageCreate.getMessageData.getText.toLowerCase.equals("ping") )
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

  case class EmailAddressEvent(emailAddress : String) extends DirectMessageEvent
  object EmailAddressEvent {
    final val extractPossibleEmailRegex = "([a-zA-Z0-9+._-]+@[a-zA-Z0-9._-]+.[a-zA-Z0-9_-]+)".r
    def unapply(event: DirectMessageEvent) = {
      val possibleEmail = {
        event.getMessageCreate.getMessageData.getText.toLowerCase match {
          case extractPossibleEmailRegex(possibleEmail) => Some(possibleEmail)
          case _ => None
        }
      }
      if( possibleEmail.isDefined && EmailValidator.getInstance().isValid(possibleEmail.get))
        Some(new EmailAddressEvent(possibleEmail.get))
      else None
    }
  }

  case class PhoneNumberEvent(phoneNumber : String) extends DirectMessageEvent
  object PhoneNumberEvent {
    import com.google.i18n.phonenumbers.PhoneNumberUtil
    import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat

    final val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance

    def unapply(event: DirectMessageEvent) = {
      val parsed = Try(phoneNumberUtil.parse(event.getMessageCreate.getMessageData.getText, null))
      val formatted = Try(phoneNumberUtil.format(parsed.get, PhoneNumberFormat.E164))
      if( formatted.isSuccess)
        Some(new PhoneNumberEvent(formatted.get))
      else None
    }
  }

  object RankEntity extends Enumeration {
    type RankEntity = Value
    val domains, hashtags, mentions, posts, friends, followers = Value
  }

  case class RankEvent(
                        val entity: RankEntity,
                        val metric: String = "count") extends DirectMessageEvent

  object RankEvent {
    val superlativeKeywords = List("list", "top", "rank", "best")
    val clauseKeywords = List("by", "for", "from")
    val metricKeywords = List("friend", "follower", "list", "tweet", "status")
    def unapply(event: DirectMessageEvent): Option[RankEvent] = {
      val text = event.getMessageCreate.getMessageData.getText.toLowerCase
      if( superlativeKeywords.exists(text.contains)) {
//        if( TopHashtags.hashtagEntityKeywords.exists(text.contains)) {
//          return Some(new RankEvent(RankEntity.hashtags))
//        } else if( TopDomains.domainEntityKeywords.exists(text.contains )) {
//          return Some(new RankEvent(RankEntity.domains))
//        } else if ( TopMentions.mentionKeywords.exists(text.contains )) {
//          return Some(new RankEvent(RankEntity.mentions))
//        } else if ( TopPosts.postEntityKeywords.exists(text.contains )) {
//          return Some(new RankEvent(RankEntity.posts))
//        } else if ( TopFollowing.followingEntityKeywords.exists(text.contains )) {
//          val entity = TopFollowing.followingEntityKeywords.find( text.contains )
//          if( clauseKeywords.exists(text.contains)) {
//            val metric = metricKeywords.find( text.contains )
//            return Some(new RankEvent(RankEntity.withName(entity.get), metric.get))
//          } else {
//            return Some(new RankEvent(RankEntity.withName(entity.get)))
//          }
//        }
      }
      None
    }
  }

}

class DirectMessageEventConsumer extends Actor with ActorLogging {

  import DirectMessageEventConsumer._

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
      if (recipientId.equals(TwitterResource.user.getIdStr)) {
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
    (event.entity, event.metric) match {
//      case (RankEntity.domains, _) => new URL(TopDomains.url.toString)
//      case (RankEntity.hashtags, _) => new URL(TopHashtags.url.toString)
//      case (RankEntity.mentions, _) => new URL(TopMentions.url.toString)
//      case (RankEntity.posts, metric) => new URIBuilder(TopFollowing.url.toString)
//                                  .addParameter("metric", metric).build().toURL
//      case (RankEntity.followers, metric) => new URIBuilder(TopFollowing.url.toString)
//                                  .addParameter("endpoint", RankEntity.followers.toString)
//                                  .addParameter("metric", metric).build().toURL
//      case (RankEntity.friends, metric) => new URIBuilder(TopFollowing.url.toString)
//                                  .addParameter("endpoint", RankEntity.friends.toString)
//                                  .addParameter("metric", metric).build().toURL
      case _ => new URL("http://idk.com")
    }
  }

  def respondTo(event: RankEvent) : MessageData = {
    response(text = "Here's a list of my top " + event.entity, Some(listURL(event)))
  }

  def respondSorry() : MessageData = {
    response(text = "sorry")
  }

  def pingResponse() : String = {
    "pong"
  }

  def response(text: String, url : Option[URL] = None ): MessageData = {
    var messageData = new MessageData()
      .withText(text)
    if (url.nonEmpty)
      messageData.setText( messageData.getText + " " + url.get.toString )
    messageData
  }

}

