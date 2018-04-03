package packageName

import java.io.{File, FilenameFilter}

object DownloadedPostHandler {
  private val infoFilesFilter = new FilenameFilter() {
    override def accept(dir: File, name: String): Boolean =
      name.startsWith(InfoFile.NameWithoutExtraction) && name.endsWith(InfoFile.Extraction)
  }

  private var onlyOneObject: Option[DownloadedPostHandler] = None

  def apply(folderPath: String): DownloadedPostHandler = {
    onlyOneObject = onlyOneObject match {
      case None => Some(new DownloadedPostHandler(folderPath))
      case _ => throw new RuntimeException("This class should be created only once.")
    }
    onlyOneObject.get
  }
}

class DownloadedPostHandler private(folderPath: String) {
  val postsInfo: Array[InfoFile] =
    new File(folderPath)
      .listFiles(_.isDirectory)
      .map(_.getAbsolutePath)
      .map(_ + '/' + InfoFile.FullName)
      .filter(new File(_).exists())
      .map(InfoFile.fromJsonFile)
      .filter(_.isDefined)
      .map(_.get)

  val postsId: Set[Int] = postsInfo.map(_.id).toSet
}
