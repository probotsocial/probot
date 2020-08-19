package social.probot.microservice

import com.fasterxml.jackson.databind.node.ObjectNode
import com.typesafe.config.Config
import com.typesafe.config.ConfigRenderOptions
import org.apache.commons.lang3.StringUtils
import org.apache.juneau.microservice.jetty.JettyMicroservice
import org.apache.juneau.microservice.jetty.JettyMicroserviceBuilder
import org.apache.juneau.microservice.resources.ConfigResource
import org.apache.juneau.microservice.resources.LogsResource
import org.apache.streams.config.StreamsConfiguration
import org.apache.streams.config.StreamsConfigurator
import org.apache.streams.jackson.StreamsJacksonMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object ProbotMicroservice {

  private final val LOGGER = LoggerFactory.getLogger(classOf[ProbotMicroservice])

  lazy val MAPPER = StreamsJacksonMapper.getInstance

  @throws[Exception]
  final def main(args: Array[String]): Unit = {
    val builder : JettyMicroserviceBuilder = JettyMicroservice
      .create()
      .args(args.head)
      .servlet(classOf[ConfigResource])
      .servlet(classOf[LogsResource])
      .servlet(classOf[ProbotResource])
    val microservice = new ProbotMicroservice(builder)
    microservice.start().join();
  }

  def logInitializing(logger: Logger, environment: Config, path: String) = {
    logger.info("Initializing {}", Array(logger.getName):_*)
    if( StringUtils.isNotBlank(path)) {
      logger.info("from {}", Array(path.toString):_*)
    }
  }

  def logEnvironment(logger: Logger, environment: Config, path: String) = {
    val tree: String = {
      if( StringUtils.isNotBlank(path) )
        environment.root().get(path).render(ConfigRenderOptions.defaults())
      else
        environment.root().render(ConfigRenderOptions.defaults())
    }
    logger.info("Environment: {}", tree )
  }

  def logInitialized(logger: Logger, bean: java.io.Serializable, variable: String) = {
    val node : ObjectNode = MAPPER.convertValue(bean, classOf[ObjectNode])
    logger.info("Initialized {} with size {}", Array(variable, node.size().toString):_*)
    val json: String = MAPPER.writeValueAsString(node)
    logger.info("JSON Value: {}", json )
  }

}

class ProbotMicroservice(builder: JettyMicroserviceBuilder) extends JettyMicroservice(builder) {

  import ProbotMicroservice._

  lazy val streamsConfiguration : StreamsConfiguration = StreamsConfigurator.detectConfiguration()
  lazy val typesafeConfiguration: Config = StreamsConfigurator.getConfig

  @throws[Exception]
  override def init(): ProbotMicroservice = {
    super.init()
    logInitializing(LOGGER, typesafeConfiguration, "")
    this;
  }

}
