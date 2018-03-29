package packageName

import java.io.File

import org.jsoup.nodes.{Document, Element}
import packageName.Downloader.{getPage, downloadPageToFile}
import packageName.FileHandling.getFileNameWithoutExtension

import scala.collection.JavaConverters._

import scala.io.Source.fromFile
import scala.util.Try

object OldAndUseless {

  def getDownloadedTags(filePath: String): Iterable[String] =
    fromFile(filePath).getLines().toList

  def downloadTagPosts(tagName: String, howMuchPostsToDownload: Int, folderPath: String): Unit = {
    val fullOutFolderPath: File = new File(folderPath + '/' + tagName)
    if (fullOutFolderPath.isDirectory)
      if (!fullOutFolderPath.exists())
        throw new IllegalArgumentException("Provided path is not a directory")

    val alreadyDownloadedPostsName: Set[String] = if (fullOutFolderPath.exists())
      fullOutFolderPath.listFiles().map(getFileNameWithoutExtension).toSet
    else Set.empty[String]
    if (alreadyDownloadedPostsName.size == howMuchPostsToDownload) {
      println(f"'$tagName' folder already has enough downloaded files. Stop downloading.")
      return
    }

    val tagUrl = getTagUrlByTagName(tagName)
    val pages = getTagPagesWithNumber(tagUrl, howMuchPostsToDownload)
    val postsUrls = pages.map(getPage).flatMap(getAllPostsUrlOnPage).take(howMuchPostsToDownload)
    val postsNameToUrl = postsUrls.map(x => getPostNameByItsUrl(x) -> x).toMap
    val postsToDownload = postsNameToUrl.filterKeys(!alreadyDownloadedPostsName.contains(_)).values
    postsToDownload.foreach(downloadPageToFile(_, fullOutFolderPath.getAbsolutePath))
  }

  def getTagUrlByTagName(name: String): String =
    f"https://pikabu.ru/search.php?st=2&r=3&t=$name"

  def getAllPostsUrlOnPage(page: Document): List[String] =
    page
      .getElementsByClass("story")
      .asScala
      .map(getPostLinkByPostItem)
      .filter(_.isDefined)
      .map(_.get)
      .toList

  def getPostLinkByPostItem(item: Element): Option[String] =
    Try(item.getElementsByClass("story__title-link").first().attr("href")).toOption

  def getTagPagesWithNumber(url: String, howMuchPostsDownloadFromOneUrl: Int = 10): List[String] = {
    val postsOnEveryPage = 20 // Imperative constant taken from the site
    val maxPageNumber: Int = Math.ceil(howMuchPostsDownloadFromOneUrl.toFloat / postsOnEveryPage).toInt
    (1 to maxPageNumber).map { x => f"$url&page=$x" }.toList
  }


  def getPostNameByItsUrl(url: String): String =
    url.split("/").last
}
