package probot.streams

import java.util.concurrent.Callable

import org.apache.juneau.annotation.BeanProperty
import org.apache.livy.Job
import org.apache.livy.JobContext
import org.apache.spark.sql.SparkSession
import org.apache.streams.twitter.config.TwitterFollowingConfiguration

class JdbcConfiguration {
  @BeanProperty var database : String = ""
  @BeanProperty var driver : String = ""
  @BeanProperty var host : String = ""
  @BeanProperty var password : String = ""
  @BeanProperty var port : String = ""
  @BeanProperty var schema : String = ""
  @BeanProperty var table : String = ""
  @BeanProperty var url : String = ""
  @BeanProperty var user : String = ""
}

class BackfillFollowersSparkJobRequest {
  @BeanProperty var input : TwitterFollowingConfiguration = _
  @BeanProperty var output : JdbcConfiguration = _
}

class BackfillFollowersSparkJobResult {
  @BeanProperty var success : Boolean = _
}

class BackfillFollowersLivyJob(request : BackfillFollowersSparkJobRequest) extends Job[BackfillFollowersSparkJobResult] {
  override def call(jobContext: JobContext): BackfillFollowersSparkJobResult = {
    new BackfillFollowersSparkJob(
      request,
      jobContext.sparkSession()
    ).call()
  }
}

object BackfillFollowersSparkJob {
  import probot.SparkResource.livyScalaClient
}

class BackfillFollowersSparkJob(
                               request : BackfillFollowersSparkJobRequest,
                               session : SparkSession
                               ) extends Callable[BackfillFollowersSparkJobResult] {

  val jdbc_options =  Map(
    "user" -> request.output.user,
    "password" -> request.output.password,
    "url" -> request.output.url,
    "driver" -> request.output.driver,
    "fetchSize" -> "100",
    "numPartitions" -> "1"
  )

  val jdbc_properties = new java.util.Properties
  jdbc_properties.setProperty("driver", request.output.driver)
  jdbc_properties.setProperty("user", request.output.user)
  jdbc_properties.setProperty("password", request.output.password)
  jdbc_properties.setProperty("stringtype", "unspecified")

  val result = new BackfillFollowersSparkJobResult()
  result.success = false

  override def call : BackfillFollowersSparkJobResult = {
    result
  }


}
