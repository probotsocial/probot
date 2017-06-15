package probot

import org.apache.streams.twitter.pojo.{DirectMessageEvent, MessageCreate, MessageData, Target, WebhookEvents}

class DirectMessageEventConsumer(event : DirectMessageEvent) extends Runnable {

  import TwitterResource._

  def processHello(in : DirectMessageEvent) = {
    val out = new DirectMessageEvent()
        .withMessageCreate(new MessageCreate()
            .withMessageData(new MessageData()
                 .withText("hello!"))
            .withTarget(new Target()
                .withRecipientId(event.getMessageCreate.getSenderId))
          .withSenderId(TwitterResource.user.getIdStr))
    TwitterResource.twitter.newEvent(out)
  }

  def processSetup(in : DirectMessageEvent) = {
    WebhookResource.consumerSecret
    WebhookResource.webhook
    WebhookResource.message
    WebhookResource.welcomeMessage
    WebhookResource.rule
    WebhookResource.subscribed
  }

  def isHello(event : DirectMessageEvent) : Boolean = {
    return event.getMessageCreate.getMessageData.getText.contains("hello")
  }

  def isSetup(event : DirectMessageEvent) : Boolean = {
    return event.getMessageCreate.getMessageData.getText.contains("setup")
  }

  override def run(): Unit = {

    // classify event
    event match {
      case ( isSetup ) => {
        println("setup")
        processSetup(event)
      }
      case ( isHello ) => {
        println("hello")
        processHello(event)
      }
      case ( _ ) => {
        println("no matches")
      }
    }

  }
}