package packageName

import java.io.{FileOutputStream, OutputStreamWriter}
import java.nio.file.Paths

import org.jsoup.nodes.Element

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Try

object Post {
  def apply(item: Element): Post = new Post(item)
}


object PostBlockType extends Enumeration {
  type PostBlockType = Value

  val Image = Value("Image")
  val Text = Value("Text")
  val Gif = Value("Gif")

  val AllLowerCaseValuesName = values.map(x => x.toString.toLowerCase() -> x).toMap

  val AllTypeClasses = AllLowerCaseValuesName.keySet.map("story-block_type_" + _)

}


protected case class PostBlock(item: Element) {
  val typeName = {
    val classes: mutable.Set[String] = item.classNames().asScala
    classes.find(PostBlockType.AllTypeClasses.contains).getOrElse(throw new MatchError(s"Cannot find known class in $classes"))
  }

  private val typeNameShort = typeName.split('_').last

  val type_ = PostBlockType.AllLowerCaseValuesName(typeNameShort)

  val content: Content = Content(this)

  def saveToFolder(fileNameWithoutExtension: String): Unit = content.saveToFile(fileNameWithoutExtension)
}

class Post(item: Element) {

  val rating: Int = item.attr("data-rating").toInt

  val id: Int = item.attr("data-story-id").toInt


  private val header =
    item.getElementsByClass("story__header").first()

  val href: String = header.getElementsByAttribute("href").first().attr("href")

  val title: String = header.text()


  private val storyBlocksContainer =
    item.getElementsByClass("story__content-inner").first()

  val storyBlocks: Seq[PostBlock] = storyBlocksContainer.getElementsByClass("story-block").asScala.map(PostBlock)


  val tags: Seq[String] = item.getElementsByClass("tags__tag").asScala.map(_.text().trim)

  private var outputFolderPath: String =
    ""

  def saveToFolder(parentFolder: String): Unit = {
    import FileHandling._

    outputFolderPath = Paths.get(parentFolder, id + "_" + replaceAllIllegalFileNameChars(title)).toFile.getAbsolutePath
    createDirIfNotExists(outputFolderPath)
    savePostInfo()
    storyBlocks.zipWithIndex.foreach { case (block, index) =>
      block.saveToFolder(outputFolderPath + f"/content_$index%02d")
    }
  }

  private def savePostInfo() = {
    val infoData = Seq(id, rating, title, tags.mkString(", ")).mkString("\n")
    FileHandling.saveDataToFile(infoData, outputFolderPath + "/info.txt")
  }
}
