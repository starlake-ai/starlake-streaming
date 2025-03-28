package ai.starlake.streaming.sink.http

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.client.methods.{HttpPost, HttpUriRequest}
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.execution.streaming.Source
import org.apache.spark.sql.sources.{DataSourceRegister, StreamSinkProvider, StreamSourceProvider}
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.types.{StringType, StructField, StructType}

import java.util.UUID

trait SinkTransformer {
  def requestUris(url: String, rows: Array[Seq[String]]): Seq[HttpUriRequest]
}

/** Display message in console */
object ConsoleSinkTransformer extends SinkTransformer {
  val mapper: ObjectMapper = Utils.newJsonMapper()
  def requestUris(url: String, rows: Array[Seq[String]]): Seq[HttpUriRequest] = {
    rows.foreach { row =>
      val jsonValue = row(1)
      println("=> " + jsonValue)
    }
    Nil
  }
}

object DefaultSinkTransformer extends SinkTransformer {
  val mapper: ObjectMapper = Utils.newJsonMapper()
  def requestUris(url: String, rows: Array[Seq[String]]): Seq[HttpUriRequest] =
    rows.map { row =>
      val jsonValue = mapper.writeValueAsString(row)
      val requestEntity =
        new StringEntity(jsonValue, ContentType.APPLICATION_JSON);
      val httpPost = new HttpPost(url)
      httpPost.setEntity(requestEntity)
      httpPost
    }
}

class HttpProvider extends StreamSinkProvider with StreamSourceProvider with DataSourceRegister {
  def createSink(
    sqlContext: SQLContext,
    parameters: Map[String, String],
    partitionColumns: Seq[String],
    outputMode: OutputMode
  ): HtttpSink = {
    new HtttpSink(parameters);
  }

  def shortName(): String = "starlake-http"

  override def sourceSchema(
    sqlContext: SQLContext,
    schema: Option[StructType],
    providerName: String,
    parameters: Map[String, String]
  ): (String, StructType) = {
    (
      parameters.getOrElse("name", UUID.randomUUID().toString),
      StructType(List(StructField("value", StringType, true)))
    )
  }

  override def createSource(
    sqlContext: SQLContext,
    metadataPath: String,
    schema: Option[StructType],
    providerName: String,
    parameters: Map[String, String]
  ): Source =
    new HttpSource(sqlContext, parameters)

}
