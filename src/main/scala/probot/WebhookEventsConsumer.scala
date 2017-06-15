package probot

import org.apache.streams.twitter.pojo.{DirectMessageEvent, WebhookEvents}

import scala.collection.JavaConversions._

import scala.concurrent.ExecutionContext.Implicits.global

class WebhookEventsConsumer(events : WebhookEvents) extends Runnable {

  def processDirectMessageEvent(event: DirectMessageEvent) = {
    global.execute(new DirectMessageEventConsumer(event))
  }

  override def run(): Unit = {

    events.getDirectMessageEvents.foreach(processDirectMessageEvent(_))

  }
}