package packageName

import java.util.Date

import packageName.Parser._
import packageName.Downloader._
import packageName.DatesHandling._

object Main extends App {

  override def main(args: Array[String]): Unit = {
    if (args.length < 1) {
      println("There is no enough console params")
      return
    }

    val outFolderPath = args(0)
    val tagsUrls = getTagsUrls.take(5)

    val today = new Date()
    val downloadFromDate = addDaysToDate(today, -5)
    val rangeOfDatesToDownload = getDatesRange(downloadFromDate, today)

    val urlToDownload: List[Seq[String]] = tagsUrls.map(getTagUrlMostPopular)
      .map { x => rangeOfDatesToDownload.map(getTagUrlByDate(x, _)) }

    urlToDownload.foreach(_.par.foreach(downloadPageToFile(_, outFolderPath)))
  }
}