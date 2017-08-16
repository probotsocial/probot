package probot

import java.util
import javax.servlet.ServletException

import org.apache.juneau.rest.annotation.{HtmlDoc, Property, RestResource}
import org.apache.juneau.rest.converters.{Traversable, Introspectable, Queryable}
import org.apache.juneau.rest.remoteable.RemoteableServlet
import org.apache.streams.twitter.api._

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

@RestResource(
  path = "/remoteable",
  htmldoc=new HtmlDoc(
    header=Array("Probot > Remoteable"),
    links=Array("options: '?method=OPTIONS'"),
    footer=Array("ASF 2.0 License")
  ),
  title = "probot.Remoteable",
  description = "probot.Remoteable",
  converters=Array(classOf[Traversable],classOf[Queryable],classOf[Introspectable]),
  properties=Array(new Property(name = "REST_allowMethodParam", value = "*"))
)
class Remoteable extends RemoteableServlet {
  import probot.TwitterResource._

  @throws[Exception]
  override def getServiceMap() : util.Map[Class[_],Object] = {
    Map[Class[_],Object](
      classOf[Twitter] -> twitter,
      classOf[Account] -> twitter,
      classOf[AccountActivity] -> twitter,
      classOf[Favorites] -> twitter,
      classOf[Friends] -> twitter,
      classOf[Geo] -> twitter,
      classOf[Media] -> twitter,
      classOf[Statuses] -> twitter,
      classOf[Users] -> twitter,
      classOf[WelcomeMessageRules] -> twitter,
      classOf[WelcomeMessages] -> twitter
    ).asJava
  }

}
