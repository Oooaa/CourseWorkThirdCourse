import java.io.{BufferedWriter, File, FileOutputStream, OutputStreamWriter}

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}

import scala.collection.JavaConverters._
import scala.util.Try
import scala.io.Source.fromFile

object Main extends App {

  def writeToFile(path: String, data: String): Unit = {
    println(path)
    val pw: BufferedWriter = new BufferedWriter(
      new OutputStreamWriter(
        new FileOutputStream(path, true),
        "windows-1251"))
    try pw.write(data) finally pw.close()
  }

  def getPage(url: String) = {
    println(url.replace(' ', '_'))
    Jsoup.connect(url).get
  }

  def getTagsNames: List[String] = {
    val tagsUrl = "https://pikabu.ru/tag"
    val tagsTableItemClassName = "b-tag-search-result"
    getPage(tagsUrl)
      .getElementsByClass(tagsTableItemClassName)
      .first
      .children
      .asScala
      .map(_.getElementsByAttribute("href").html())
      .filter(!_.isEmpty)
      .toSet
      .toList
  }

  def getTagUrlByTagName(name: String): String =
    f"https://pikabu.ru/search.php?st=2&r=3&t=$name"

  def getPostLinkByPostItem(item: Element): Option[String] =
    Try(item.getElementsByClass("story__title-link").first().attr("href")).toOption

  def getAllPostsUrlOnPage(page: Document): List[String] =
    page
      .getElementsByClass("story")
      .asScala
      .map(getPostLinkByPostItem)
      .filter(_.isDefined)
      .map(_.get)
      .toList

  def getTagPagesWithNumber(url: String, howMuchPostsDownloadFromOneUrl: Int = 10): List[String] = {
    val postsOnEveryPage = 20 // Imperative constant taken from the site
    val maxPageNumber: Int = Math.ceil(howMuchPostsDownloadFromOneUrl.toFloat / postsOnEveryPage).toInt
    (1 to maxPageNumber).map { x => f"$url&page=$x" }.toList
  }

  def createDirIfNotExists(path: String): Unit =
    new File(path).mkdirs()


  def downloadPageToFile(url: String, folderPath: String): Unit = {
    val page = getPage(url)
    val fileName = url.split("/").last
    createDirIfNotExists(folderPath)
    writeToFile(folderPath + "/" + fileName + ".html", page.toString)
  }

  def deleteFileIfExists(path: String): Unit = {
    val fileTemp = new File(path)
    if (fileTemp.exists())
      fileTemp.delete()
  }

  def getDownloadedTags(filePath: String): Iterable[String] =
    fromFile(filePath).getLines().toList

  def getPostNameByItsUrl(url: String): String =
    url.split("/").last


  def getFileNameWithoutExtension(file: File): String =
    file.getName.takeWhile(_ != '.')

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

  override def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      println("There is no enough console params")
      return
    }

    val everyTagPostsCount = args(0).toInt
    val outFolderPath = args(1)

    val tagsNames = getTagsNames.sorted

    tagsNames.foreach(downloadTagPosts(_, everyTagPostsCount, outFolderPath))
  }


}
