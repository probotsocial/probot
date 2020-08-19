package social.probot.actors

import org.apache.commons.lang3.StringUtils
import org.apache.juneau.ObjectList
import org.apache.juneau.ObjectMap
import org.apache.juneau.http.annotation.Body
import org.apache.juneau.http.annotation.Header
import org.apache.juneau.json.JsonParser
import org.apache.juneau.json.JsonSerializer
import org.apache.juneau.remote.RemoteInterface
import org.apache.juneau.rest.client.RestClient
import org.apache.juneau.rest.client.remote.RemoteMethod
import org.apache.streams.components.http.HttpConfiguration
import org.apache.streams.config.ComponentConfigurator
import org.apache.streams.config.StreamsConfigurator
import org.slf4j.LoggerFactory

object ProbotPersistance {
  private final val LOGGER = LoggerFactory.getLogger(classOf[ProbotPersistance])

  val config = StreamsConfigurator.getConfig.getConfig("ProbotPersistance")
  val client = new ComponentConfigurator[HttpConfiguration](classOf[HttpConfiguration]).detectConfiguration(config)
  val url = client.getProtocol+"://"+client.getHostname+":"+client.getPort
  val restClientBuilder = RestClient.create()
    .parser(JsonParser.DEFAULT)
    .rootUrl(url)
    .serializer(JsonSerializer.DEFAULT)
    .trimEmptyCollections(true)
    .trimEmptyMaps(true);
  val withAuth = {
    if( StringUtils.isNotBlank(client.getUsername) && StringUtils.isNotBlank(client.getPassword))
      restClientBuilder.basicAuth(client.getHostname, client.getPort.intValue(), client.getUsername, client.getPassword)
    else restClientBuilder
  }
  val restClient = withAuth.build()
  val probotPersistance = restClient.getRemoteResource(classOf[ProbotPersistance])

}

@RemoteInterface
trait ProbotPersistance {

  @RemoteMethod (method = "POST", path = "/direct_message_event")
  def persistDirectMessageEvent ( @Body insert : ObjectMap, @Header("Prefer") prefer : String = "resolution=ignore-duplicates" )

  @RemoteMethod (method = "POST", path = "/follower")
  def persistProfiles ( @Body insert : ObjectList, @Header("Prefer") prefer : String = "resolution=merge-duplicates"  )

  @RemoteMethod (method = "POST", path = "/opt_in")
  def persistOptIn ( @Body insert : ObjectMap, @Header("Prefer") prefer : String = "resolution=merge-duplicates" )

}
