package packageName

import java.util.Date

import org.jsoup.nodes.Document

import scala.collection.JavaConverters._
import scala.util.Try

object Parser {

  def getTagsUrls: List[String] = {
    val tagsUrl = "https://pikabu.ru/tag"
    val tagsTableItemClassName = "tags-search__section"
    Downloader.getPage(tagsUrl)
      .getElementsByClass(tagsTableItemClassName)
      .first
      .children()
      .asScala
      .map(_.attr("href"))
      .filter(_.nonEmpty)
      .toList
  }


  def addParamToUrl(url: String, paramName: String, paramValue: String): String =
    url + (if (url.contains('?')) "&" else "?") + s"$paramName=$paramValue"


  val beginningDate = new Date(108, 0, 1)

  def getTagUrlByDate(mainUrl: String, date: Date): String = {
    val daysFromBeginning = DatesHandling.getDaysBetweenDate(beginningDate, date)
    addParamToUrl(mainUrl, "d", daysFromBeginning.toString)
  }

  def getTagUrlByDate(mainUrl: String, dates: (Date, Date)): String = {
    val from = DatesHandling.getDaysBetweenDate(beginningDate, dates._1)
    val until = DatesHandling.getDaysBetweenDate(beginningDate, dates._2)
    val tmp = addParamToUrl(mainUrl, "d", from.toString)
    addParamToUrl(tmp, "D", until.toString)
  }

  def getTagUrlMostPopular(url: String): String =
    addParamToUrl(url, "st", "2")

  def getPostsOnPage(page: Document): Seq[Post] =
    page.select("article[data-rating]")
      .asScala
      .map(x => Try(Post(x)))
      .filter(x => {
        if (x.isFailure)
          println(page.baseUri() + "\n" + x.toString + "\n\n")
        x.isSuccess
      }).map(_.get)
}

