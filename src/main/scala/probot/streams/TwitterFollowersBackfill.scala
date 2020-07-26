package probot.streams

import org.apache.juneau.json.JsonSerializer
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.Encoders
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.catalyst.ScalaReflection
import org.apache.spark.sql.execution.streaming.LongOffset
import org.apache.spark.sql.execution.streaming.Offset
import org.apache.spark.sql.execution.streaming.Source
import org.apache.spark.sql.types.StructType
import org.apache.streams.config.ComponentConfigurator
import org.apache.streams.core.StreamsDatum
import org.apache.streams.twitter.config.TwitterFollowingConfiguration
import org.apache.streams.twitter.pojo.Follow
import org.apache.streams.twitter.pojo.User
import org.apache.streams.twitter.provider.TwitterFollowingProvider

import scala.collection.JavaConversions._

object TwitterFollowersBackfill {
  final val UserEncoder = Encoders.bean(classOf[User])
  final val UserSchema = ScalaReflection.schemaFor[User].dataType.asInstanceOf[StructType]
}

class TwitterFollowersBackfill(sqlContext: SQLContext) extends Source {

  private var offset: LongOffset = LongOffset(-1)

  private val twitterFollowingProviderConfiguration : TwitterFollowingConfiguration
    = new ComponentConfigurator[TwitterFollowingConfiguration](classOf[TwitterFollowingConfiguration]).detectConfiguration

  private val twitterFollowingProvider = new TwitterFollowingProvider(twitterFollowingProviderConfiguration)

  twitterFollowingProvider.prepare(twitterFollowingProviderConfiguration)

  twitterFollowingProvider.startStream()

  override def schema: StructType = TwitterFollowersBackfill.UserSchema

  override def getOffset: Option[Offset] = this.synchronized {
    println(s"getOffset: $offset")
    if (offset.offset == -1) None else Some(offset)
  }

  override def getBatch(start: Option[Offset], end: Offset): DataFrame = this.synchronized {
    println(s"getBatch: $offset")
    val currentQueue = twitterFollowingProvider.readCurrent().getQueue
    val currentQueueItems = currentQueue.map((x: StreamsDatum) => x.getDocument.asInstanceOf[Follow])
    val beanSeq = currentQueueItems.map((x: Follow) => x.getFollower).seq
    val jsonList = beanSeq.map(JsonSerializer.DEFAULT.serialize(_)).toList
    val jsonRdd = sqlContext.sparkContext.parallelize(jsonList)
    val dataFrame = sqlContext.read.format("json").json(jsonRdd)
    dataFrame
  }

  override def commit(end: Offset): Unit = ???

  override def stop(): Unit = ???
}
