package packageName

import java.nio.file.{FileAlreadyExistsException, Paths}

import org.jsoup.nodes.Element

import scala.collection.JavaConverters._
import scala.util.Try

object Post {
  def apply(item: Element): Post = new Post(item)
}

object PostBlock {
  def apply(item: Element): PostBlock =
    new PostBlock(Content(item))
}

protected case class PostBlock private(content: Content) {
  def saveToFolder(fileNameWithoutExtension: String): Unit = content.saveToFile(fileNameWithoutExtension)
}

class Post(item: Element) {

  val rating: Int = item.attr("data-rating").toInt

  val id: Int = item.attr("data-story-id").toInt



  private val header =
    item.getElementsByClass("story__header").first()

  val href: String = header.getElementsByAttribute("href").first().attr("href")

  val title: String = header.text()
  log.info(f"Paring post href: $href")

  private val storyBlocksContainer =
    item.getElementsByClass("story__content-inner").first()

  val storyBlocks: Seq[PostBlock] = storyBlocksContainer
    .getElementsByClass("story-block")
    .asScala
    .map(x => Try(PostBlock(x)))
    .filter { x =>
      if (x.isFailure)
        log.warn(s"Problems with block: $href ${x.failed.get}")
      x.isSuccess
    }
    .map(_.get)


  val tags: Seq[String] = item.getElementsByClass("tags__tag").asScala.map(_.text().trim)

  private var outputFolderPath: String = ""

  def saveToFolder(parentFolder: String): Unit = {
    import FileHandling._

    outputFolderPath = Paths.get(parentFolder, id.toString).toFile.getAbsolutePath
    createDirIfNotExists(outputFolderPath)
    storyBlocks.zipWithIndex.foreach { case (block, index) =>
      Try(block.saveToFolder(outputFolderPath + f"/content_$index%02d")) recover {
        case ex: FileAlreadyExistsException => log.warn(s"File already exist: ${ex.getFile}")
      }
    }
    savePostInfo()
  }

  private def savePostInfo(): Unit = {
    val infoFile = InfoFile(
      href = href,
      id = id,
      title = title,
      rating = rating,
      tags = tags
    )
    val infoString = InfoFile.toJson(infoFile)
    log.info(f"saving ${href} to file")
    val outputFileName = outputFolderPath + '/' + InfoFile.FullName
    FileHandling.writeToFile(
      path = outputFileName,
      data = infoString,
      encoding = "UTF-8",
      toPrint = false)
  }
}
