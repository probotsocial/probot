package probot.streams

import org.apache.spark.sql.Encoders
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.catalyst.ScalaReflection
import org.apache.spark.sql.execution.streaming.Source
import org.apache.spark.sql.sources.DataSourceRegister
import org.apache.spark.sql.sources.StreamSourceProvider
import org.apache.spark.sql.types.StructType
import org.apache.streams.twitter.pojo.User

class TwitterFollowersBackfillSourceProvider extends StreamSourceProvider with DataSourceRegister {
  import TwitterFollowersBackfill._

  override def sourceSchema(sqlContext: SQLContext, schema: Option[StructType], providerName: String, parameters: Map[String, String]): (String, StructType) = {
    (shortName(), UserSchema)
  }

  override def createSource(sqlContext: SQLContext, metadataPath: String, schema: Option[StructType], providerName: String, parameters: Map[String, String]): Source = {
    new TwitterFollowersBackfill(sqlContext)
  }

  override def shortName(): String = "TwitterFollowersBackfill"
}
