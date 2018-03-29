package packageName

import java.util.Date

import packageName.DatesHandling._
import packageName.Downloader.getPage
import packageName.FileHandling.{createDirIfNotExists, writeToFile}

import scala.collection.JavaConverters._

object Parser {

  def getTagsUrls: List[String] = {
    val tagsUrl = "https://pikabu.ru/tag"
    val tagsTableItemClassName = "tags-search__section"
    getPage(tagsUrl)
      .getElementsByClass(tagsTableItemClassName)
      .first
      .children
      .asScala
      .map(_.getElementsByClass("tags__tag"))
      .map(_.attr("href"))
      .filter(_.nonEmpty)
      .toSet
      .toList
  }


  def addParamToUrl(url: String, paramName: String, paramValue: String): String =
    url + (if (url.contains('?')) "&" else "?") + s"$paramName=$paramValue"



  def getTagUrlByDate(mainUrl: String, date: Date): String = {
    val beginningDate = new Date(108, 0, 1)
    val daysFromBeginning = getDaysBetweenDate(beginningDate, date)
    addParamToUrl(mainUrl, "d", daysFromBeginning.toString)
  }

  def getTagUrlMostPopular(url: String): String =
    addParamToUrl(url, "st", "2")
}

