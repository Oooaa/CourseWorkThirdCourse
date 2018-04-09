import com.typesafe.scalalogging.Logger

import scala.concurrent.ExecutionContext

package object packageName {
  val log = Logger("LoggerName")
  //implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))
  implicit val ec = ExecutionContext.global
}