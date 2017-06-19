package probot

import java.net.URL

import org.apache.http.client.utils.URIBuilder
import org.apache.streams.twitter.Url
import org.apache.streams.twitter.api.MessageCreateRequest
import org.apache.streams.twitter.pojo.{DirectMessageEvent, Entities, MessageCreate, MessageData, Target, WebhookEvents}

import scala.collection.JavaConverters._

class DirectMessageEventConsumer(val event : DirectMessageEvent) extends Runnable {

  import TwitterResource._

  case class HelloEvent()
  case class NotHelloEvent()

  def isHello(event : DirectMessageEvent) : Either[HelloEvent,NotHelloEvent] = {
    if( event.getMessageCreate.getMessageData.getText.contains("hello"))
      Left(new HelloEvent)
    else
      Right(new NotHelloEvent)
  }

  case class EchoEvent(text : String)
  case class NotEchoEvent()

  def isEcho(event : DirectMessageEvent) : Either[EchoEvent, NotEchoEvent] = {
    val echoKeywords = List("foo", "bar")
    if( echoKeywords.contains(event.getMessageCreate.getMessageData.getText))
      Left(new EchoEvent(event.getMessageCreate.getMessageData.getText))
    else
      Right(new NotEchoEvent)
  }

  case class PingEvent()
  case class NotPingEvent()

  def isPing(event : DirectMessageEvent) : Either[PingEvent,NotPingEvent] = {
    if( event.getMessageCreate.getMessageData.getText.equals("ping") )
      Left(new PingEvent)
    else
      Right(new NotPingEvent)
  }

  case class ListEvent(field : String)
  case class NotListEvent()

  def isList(event : DirectMessageEvent) : Either[ListEvent,NotListEvent] = {
    val listKeywords = List("list", "top", "rank", "best")
    if( listKeywords.exists(event.getMessageCreate.getMessageData.getText.contains)) {
      if( event.getMessageCreate.getMessageData.getText.contains("hashtag") ) {
        return Left(new ListEvent("hashtags"))
      }
    }
    Right(new NotListEvent)
  }

  def processHello(event : HelloEvent) = {
    sendText("hello!")
  }

  def processPing(event: PingEvent) = {
    sendText(pingResponse())
  }

  def processEcho(event: EchoEvent) = {
    sendText(event.text)
  }

  def sendUrl(text: String, url: URL) = {
    val out = new MessageCreateRequest()
      .withEvent(new DirectMessageEvent()
        .withMessageCreate(new MessageCreate()
          .withMessageData(new MessageData()
            .withText(text + ": " + url.toString))
          .withTarget(new Target()
            .withRecipientId(event.getMessageCreate.getSenderId))
          .withSenderId(TwitterResource.user.getIdStr)))
    TwitterResource.twitter.newEvent(out)
  }

  def listURL(event: ListEvent): URL = {
    new URL(TopHashtags.url.toString)
//    if( event.field.contains("hashtags")) {
//
//    }
  }

  def processList(event: ListEvent) = {
    sendUrl("Here's a list of my top "+event.field, listURL(event))
  }

  def processSorry(event: DirectMessageEvent) = {
    sendText("sorry")
  }

  def pingResponse() : String = {
    "pong"
  }

  def sendText(text : String) = {
    val out = new MessageCreateRequest()
        .withEvent(new DirectMessageEvent()
          .withMessageCreate(new MessageCreate()
        .withMessageData(new MessageData()
          .withText(text))
        .withTarget(new Target()
          .withRecipientId(event.getMessageCreate.getSenderId))
        .withSenderId(TwitterResource.user.getIdStr)))
    TwitterResource.twitter.newEvent(out)
  }

  override def run(): Unit = {

    if( event.getMessageCreate.getTarget.getRecipientId.equals(user.getIdStr)) {
      // classify event
      isHello(event) match {
        case Left(x: HelloEvent) => {
          processHello(x)
          return
        }
        case Right(x: NotHelloEvent) => {

        }
      }
      isPing(event) match {
        case Left(x: PingEvent) => {
          processPing(x)
          return
        }
        case Right(x: NotPingEvent) => {

        }
      }
      isEcho(event) match {
        case Left(x: EchoEvent) => {
          processEcho(x)
          return
        }
        case Right(x: NotEchoEvent) => {

        }
      }
      isList(event) match {
        case Left(x: ListEvent) => {
          processList(x)
          return
        }
        case Right(x: NotListEvent) => {

        }
      }
      processSorry(event)
    }

  }
}

