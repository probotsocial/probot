package probot

import java.io.File
import java.util.logging.Level

import org.apache.commons.lang3.StringUtils
import org.apache.juneau.rest.BasicRestServletGroup
import org.apache.juneau.rest.annotation.HtmlDoc
import org.apache.juneau.rest.annotation.RestResource
import org.apache.juneau.rest.annotation.HookEvent
import org.apache.juneau.rest.annotation.RestHook
import org.apache.juneau.rest.RestRequest
import org.apache.juneau.rest.RestResponse
import org.apache.livy.LivyClient
import org.apache.livy.LivyClientBuilder
import org.apache.livy.scalaapi.LivyScalaClient
import org.apache.streams.pojo.json.Activity
import org.joda.time.DateTime

object SparkResource {

  val livyClient = new LivyClientBuilder().build();
  val livyScalaClient = new LivyScalaClient(livyClient);

  val uberJar : File = new File("./dist/probot-jar-with-dependencies.jar")
  val uberJarUploadFuture = livyClient.uploadFile(uberJar)

  val uberJarUpload = uberJarUploadFuture.get()

}

@RestResource(
  defaultRequestHeaders = Array("Accept: text/html"),
  defaultResponseHeaders = Array("Content-Type: text/html"),
  htmldoc=new HtmlDoc(
    header=Array("Probot"),
    footer=Array("ASF 2.0 License")
  ),
  path = "/spark",
  title = Array("spark"),
  description = Array("spark")
)
class SparkResource extends BasicRestServletGroup {

  import SparkResource._

  def postBackfillFollowersSparkJob = {

  }
}
