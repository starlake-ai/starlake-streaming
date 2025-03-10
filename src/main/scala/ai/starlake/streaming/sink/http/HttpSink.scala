package ai.starlake.streaming.sink.http

import com.typesafe.scalalogging.StrictLogging
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.execution.streaming.Sink

import java.io.{PrintWriter, StringWriter}
import scala.util.{Failure, Success, Try}

class HtttpSink(parameters: Map[String, String]) extends Sink with StrictLogging {
  private val numRetries: Int = parameters.getOrElse("numRetries", "3").toInt
  private val retryInterval: Int = parameters.getOrElse("retryInterval", "1000").toInt

  val client = new HttpSinkClient(parameters)
  def exceptionAsString(exception: Throwable): String = {
    val sw = new StringWriter
    exception.printStackTrace(new PrintWriter(sw))
    sw.toString
  }
  override def addBatch(batchId: Long, data: DataFrame): Unit = {
    var success = false;
    var retried = 0;
    while (!success && retried < numRetries) {
      Try {
        retried += 1;
        client.send(data);
        success = true;
      } match {
        case Failure(e) =>
          success = false;
          logger.warn(exceptionAsString(e));
          if (retried < numRetries) {
            val sleepTime = retryInterval * retried;
            logger.warn(s"will retry to send after ${sleepTime}ms");
            Thread.sleep(sleepTime);
          } else {
            throw e;
          }
        case Success(_) =>
      }
    }
  }
}
