package packageName

object Main extends App {

  override def main(args: Array[String]): Unit = {
    if (args.length < 1) {
      println("There is no enough console params")
      return
    }

    val outFolderPath = args(0)
    val tagsUrls = Parser.getTagsUrls.take(3)

    val lastDates = DatesHandling.getLastDaysRange(5)

    val urlsToDownload: List[String] = tagsUrls.map(Parser.getTagUrlMostPopular)
      .flatMap { x => lastDates.map(Parser.getTagUrlByDate(x, _)) }

    urlsToDownload
      .map(Downloader.getPage)
      .flatMap(Parser.getPostsOnPage)
      .foreach(_.saveToFolder(outFolderPath))
  }
}