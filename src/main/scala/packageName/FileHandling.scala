package packageName

import java.io._
import java.nio.file.{Files, Paths}

object FileHandling {

  def writeStreamToFile(stream: InputStream, filePath: String): Unit =
    Files.copy(stream, Paths.get(filePath))

  def deleteFileIfExists(path: String): Unit = {
    val fileTemp = new File(path)
    if (fileTemp.exists())
      fileTemp.delete()
  }

  def writeToFile(path: String, data: String, encoding: String = "windows-1251"): Unit = {
    println(path)
    val pw: BufferedWriter = new BufferedWriter(
      new OutputStreamWriter(
        new FileOutputStream(path, true),
        encoding))
    try pw.write(data) finally pw.close()
  }

  def createDirIfNotExists(path: String): Unit =
    new File(path).mkdirs()

  def getFileNameWithoutExtension(file: File): String =
    file.getName.takeWhile(_ != '.')
}
