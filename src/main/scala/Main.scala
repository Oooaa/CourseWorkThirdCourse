import java.io.{BufferedWriter, File, FileOutputStream, OutputStreamWriter}

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try
import scala.concurrent.forkjoin.ForkJoinPool
import scala.io.Source.fromFile

object Main extends App {

  def writeToFile(path: String, data: String): Unit = {
    //    println(path)
    val pw: BufferedWriter = new BufferedWriter(
      new OutputStreamWriter(
        new FileOutputStream(path, true),
        "UTF-8"))
    try pw.write(data) finally pw.close()
  }

  def getPage(url: String) = {
    println(url.replace(' ', '_'))
    Jsoup.connect(url).get
  }

  def getTagsNames(): List[String] = {
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

  def addPagesNumberToUrl(url: String, howMuchPostsDownloadFromOneUrl: Int = 10): List[String] = {
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
    if (fileTemp.exists()) {
      fileTemp.delete()
    }
  }

  def getDownloadedTags(filePath: String): Iterable[String] =
    fromFile(filePath).getLines().toList

  override def main(args: Array[String]): Unit = {
    //    val OUTPUT_DIRECTORY = "/Users/Air_Folder/Downloads/CourseWorkTrirdCourse/Output/"
    //    val OUTPUT_DIRECTORY = args(1)
    //    val NUMBER_OF_THREADS = args(0)
    //    print(f"Output dir: $OUTPUT_DIRECTORY\n")
    //    print(f"Number of threads: $NUMBER_OF_THREADS\n")
    //    val url = "https://pikabu.ru/best/"

    val tagsNames = getTagsNames().sorted

//    println(tagsNames.sortBy(_.length).takeRight(10).map(x => x.length + " : " + x).mkString("\n"))

    println(f"All tages count: ${tagsNames.size}")

    val downloadedTags = getDownloadedTags("/Users/Air_Folder/jupyter/downloadedTags.txt").toSet
    println(s"Dropped tags count: ${downloadedTags.size}")
    val tagsUrl: Map[String, String] = tagsNames.map(x => x -> getTagUrlByTagName(x)).toMap.filter(x => !downloadedTags.contains(x._1))
    println(s"Tags to download count: ${tagsUrl.size}")

    val tagsPagesUrl: Map[String, List[String]] = tagsUrl.mapValues(addPagesNumberToUrl(_, 100))

    implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(new ForkJoinPool(4))
    val filePath = "/Users/Air_Folder/Downloads/CourseWorkTrirdCourse/postsLinks.txt"
    deleteFileIfExists(filePath)
    val tagsPostsUrl = tagsPagesUrl.map { case (tagName, pageUrl: Seq[String]) =>
      Future {
        pageUrl.foreach(pageUrl =>
          getAllPostsUrlOnPage(getPage(pageUrl)).foreach(x => writeToFile(filePath, f"$tagName\t$x\n")))
      }

    }

    //    val pageDownloadFutures = tagsPostsUrl.map(
    //      _.map {
    //        _.foreach {
    //          case (tagName, postsUrl: Seq[String]) =>
    //            postsUrl.foreach(
    //              downloadPageToFile(_, OUTPUT_DIRECTORY + tagName)
    //            )
    //        }
    //      }
    //    )


    val future = Future.sequence(tagsPostsUrl)
    Await.ready(future, Duration.Inf)
    //    val res = Await.result(future, Duration.Inf).flatten.toList.sortBy(_._1)
    //    val out = res.flatMap { case (tagName, lstOfLinks) => lstOfLinks.sorted.map(tagName + "\t" + _) }.mkString("\n")

    //      .map(x => x._1 + "\t" + x._2.sorted.mkString("\t")).toSet.toList.sorted.mkString("\n")
    //    writeToFile(filePath, out)
  }


}
