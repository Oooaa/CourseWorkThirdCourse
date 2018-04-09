package packageName

import java.util.Calendar

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object Main extends App {

  override def main(args: Array[String]): Unit = {
    if (args.length < 4)
      throw new ArrayIndexOutOfBoundsException("There is no enough console params")

    val outFolderPath = args(0)
    val toDownloadTopTagsCount = args(1).toInt
    val howMuchRanges = args(2).toInt
    val oneRangeAmount = args(3).toInt
    val oneRangeUnit = Calendar.MONTH

    val tagsUrls = Parser.getTagsUrls.take(toDownloadTopTagsCount)
    val lastDates = DatesHandling.generateRanges(howMuchRanges, oneRangeAmount, oneRangeUnit)

    val urlsToDownload: List[String] = tagsUrls.map(Parser.getTagUrlMostPopular)
      .flatMap { x => lastDates.map(Parser.getTagUrlByDate(x, _)) }

    val downloadedPostIds = DownloadedPostHandler(outFolderPath).postsId

    val futures = urlsToDownload
        .view
      .map(x => Future(Downloader.getPage(x)))
      .map(_.map(Parser.getPostsOnPage))
      .map(_.map { seqOfIds =>
        val setOfIds: Set[Int] = seqOfIds.map(_.id).toSet
        val alreadyDownloaded = setOfIds & downloadedPostIds
        log.info(s"Already downloaded posts ids: ${alreadyDownloaded.mkString(", ")}")
        seqOfIds.filterNot(x => downloadedPostIds.contains(x.id))
      })
      .map(_.map(_.map(_.saveToFolder(outFolderPath))))

    Await.result(Future.sequence(futures), Duration.Inf)
  }
}