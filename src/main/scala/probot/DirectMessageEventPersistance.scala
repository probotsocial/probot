package probot

import org.apache.juneau.ObjectMap
import org.apache.juneau.http.annotation.Body
import org.apache.juneau.remote.RemoteInterface
import org.apache.juneau.rest.client.remote.RemoteMethod

@RemoteInterface
trait DirectMessageEventPersistance {

  @RemoteMethod (method = "POST", path = "/direct_message_event")
  def persistDirectMessageEvent ( @Body insert : ObjectMap )

}
