package packageName

import java.io._
import java.nio.file._

object FileHandling {

  def writeStreamToFile(stream: InputStream, filePath: String): Unit =
    Files.copy(stream, Paths.get(filePath))

  def deleteFileIfExists(path: String): Unit = {
    val fileTemp = new File(path)
    if (fileTemp.exists())
      fileTemp.delete()
  }

  def writeToFile(path: String, data: String, encoding: String = "windows-1251", toPrint: Boolean = true): Unit = {
    if (toPrint)
      println(path)
    val pw: BufferedWriter = new BufferedWriter(
      new OutputStreamWriter(
        new FileOutputStream(path, true),
        encoding))
    try pw.write(data) finally pw.close()
  }

  def createDirIfNotExists(path: String): Unit = new File(path).mkdirs()

  def getFileNameWithoutExtension(file: File): String = file.getName.takeWhile(_ != '.')

  def getExtension(fileName: String): String = {
    val lastIndexOfDot = fileName.lastIndexOf('.')
    fileName.drop(lastIndexOfDot + 1)
  }

  def replaceAllIllegalFileNameChars(name: String): String = name.replaceAll("[^a-zA-Zа-яА-Я0-9\\-]", "_").replaceAll("_+", "_")

  def saveDataToFile(inputStream: String, file: String): Long =
    saveDataToFile(new ByteArrayInputStream(inputStream.getBytes()), file)

  def saveDataToFile(inputStream: InputStream, file: String): Long =
    saveDataToFile(inputStream, Paths.get(file))

  def saveDataToFile(inputStream: InputStream, file: Path): Long = {
    Files.copy(inputStream, file, StandardCopyOption.REPLACE_EXISTING)
  }
}
