package packageName

import java.io.{ByteArrayInputStream, InputStream}

import org.jsoup.nodes.Element
import scala.collection.JavaConverters._

abstract class Content protected(contentContainerBlock: Element) {
  val outputFileExtension: String

  val value: InputStream

  def saveToFile(filePathWithoutExtension: String): Unit = {
    val path = filePathWithoutExtension + "." + outputFileExtension
    FileHandling.saveDataToFile(value, path)
  }
}

object Content {
  def apply(item: Element): Content =
    item.classNames().asScala.collectFirst {
      case "story-block_type_image" => new ImageContent(item)
      case "story-block_type_text" => new TextContent(item)
    }.getOrElse{
      log.info(s"Not found known class in ${item.classNames()}")
      new NonParsableContent(null)
    }
}

class TextContent(contentContainerBlock: Element) extends Content(contentContainerBlock) {
  override val outputFileExtension = "txt"

  override lazy val value = new ByteArrayInputStream(contentContainerBlock.text().getBytes())
}

class ImageContent(contentContainerBlock: Element) extends Content(contentContainerBlock) {
  private val imageItem: Element = contentContainerBlock.getElementsByClass("story-image__content").first().child(0)
  private val imageUrl = imageItem.select("img").attr("data-large-image")

  override val outputFileExtension: String = FileHandling.getExtension(imageUrl)
  override lazy val value: InputStream = Downloader.getImageStream(imageUrl)
}

class NonParsableContent(contentContainerBlock: Element) extends Content(contentContainerBlock) {
  override val outputFileExtension = "unknown"

  override lazy val value: Null = null
}