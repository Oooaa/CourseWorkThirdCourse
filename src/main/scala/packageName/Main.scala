package packageName

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object Main extends App {

  override def main(args: Array[String]): Unit = {
    if (args.length < 3) {
      println("There is no enough console params")
      return
    }

    val outFolderPath = args(0)
    val toDownloadTopTags = args(1).toInt
    val howMuchLastDays = args(2).toInt

    val tagsUrls = Parser.getTagsUrls.take(toDownloadTopTags)
    val lastDates = DatesHandling.getLastDaysRange(howMuchLastDays)

    val urlsToDownload: List[String] = tagsUrls.map(Parser.getTagUrlMostPopular)
      .flatMap { x => lastDates.map(Parser.getTagUrlByDate(x, _)) }

    val a = urlsToDownload
      .map(x => Future(Downloader.getPage(x)))
      .map(_.map(Parser.getPostsOnPage))
      .map(_.map(_.map(_.saveToFolder(outFolderPath))))

    Await.result(Future.sequence(a), Duration.Inf)
  }
}