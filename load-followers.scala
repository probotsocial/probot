import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.DataFrame
final val sparkSession = SparkSession.builder().getOrCreate()
class Request {
  val baseDir = "/workdir"
  val inputPath = "collect-followers.jsonl"
  val inputFormat = "json"
  val jdbcDriver = "org.postgresql.Driver"
  val jdbcUser = "postgres"
  val jdbcPassword = "postgres"
  val jdbcUrl = "jdbc:postgresql://postgres:5432/postgres"
}
val request = new Request
val follows_jsons : DataFrame = sparkSession.sqlContext.read.format("json").load(request.baseDir + "/" + request.inputPath)
follows_jsons.createOrReplaceTempView("follows_jsons")
val profiles_df = sparkSession.sqlContext.sql(
  """
    select
    concat('twitter:', follower.id_str) as id,
    follower.name as as_name,
    follower.description as as_summary,
    follower.url as as_url,
    follower.location as as_location_name,
    follower.screen_name as apst_handle,
    follower.followers_count as apst_followers,
    follower.friends_count as apst_friends,
    follower.favourites_count as apst_likes,
    follower.statuses_count as apst_posts,
    follower.listed_count as apst_lists,
    follower.blocking as twitter_blocking,
    follower.following as twitter_following,
    follower.follow_request_sent as twitter_followrequestsent
from follows_jsons
    """
)
val jdbc_options =  Map(
  "user" -> request.jdbcUser,
  "password" -> request.jdbcPassword,
  "url" -> request.jdbcUrl,
  "driver" -> request.jdbcDriver,
  "fetchSize" -> "100",
  "numPartitions" -> "1"
)
val jdbc_properties = new java.util.Properties
jdbc_properties.setProperty("driver", request.jdbcDriver)
jdbc_properties.setProperty("user", request.jdbcUser)
jdbc_properties.setProperty("password", request.jdbcPassword)
jdbc_properties.setProperty("stringtype", "unspecified")
profiles_df.count()
profiles_df.write.mode("overwrite").jdbc(request.jdbcUrl, "followers", jdbc_properties)
