package packageName

import java.io._
import java.net.URL

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import packageName.FileHandling.{createDirIfNotExists, writeToFile}


object Downloader {
  def getImageStream(imageUrl: String): InputStream =
    new URL(imageUrl).openStream()


  def getPage(url: String): Document = {
    println(url.replace(' ', '_'))
    Jsoup.connect(url).get
  }

  def downloadPageToFile(url: String, folderPath: String): Unit = {
    val page = getPage(url)
    val fileName = url.split("/").last
    createDirIfNotExists(folderPath)
    writeToFile(folderPath + "/" + fileName + ".html", page.toString)
  }

}
