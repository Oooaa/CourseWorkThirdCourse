package packageName

import java.io.{ByteArrayInputStream, InputStream}
import java.nio.file.{Files, Paths}

import org.jsoup.nodes.Element

abstract class Content protected(contentContainerBlock: Element) {
  val outputFileExtension: String

  val value: InputStream

  def saveToFile(filePathWithoutExtension: String): Unit = {
    val path = filePathWithoutExtension + "." + outputFileExtension
    FileHandling.saveDataToFile(value, path)
  }
}

object Content {
  def apply(block: PostBlock): Content = block.type_ match {
    case PostBlockType.Image => new ImageContent(block.item)
    case PostBlockType.Text => new TextContent(block.item)
    case PostBlockType.Gif =>  new ImageContent(block.item) //throw new NotImplementedError()
    case _ => throw new MatchError(s"Type '${block.typeName}' does not expected")
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