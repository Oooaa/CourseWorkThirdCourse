package packageName

import org.json4s.{DefaultFormats, Extraction => JsonExtraction}
import org.json4s.jackson.JsonMethods._

import scala.io.Source
import scala.util.Try


final case class InfoFile(href: String, id: Int, rating: Int, title: String, tags: Seq[String])

object InfoFile {
  implicit val formats: DefaultFormats.type = DefaultFormats

  val NameWithoutExtraction = "info"
  val Extraction = "json"
  val FullName: String = NameWithoutExtraction + '.' + Extraction

  def toJson(infoFile: InfoFile): String =
    pretty(render(JsonExtraction.decompose(infoFile)))


  def fromJsonFile(filePath: String): Option[InfoFile] = {
    Try {
      val json = Source.fromFile(filePath).getLines().mkString(" ")
      val res = parse(json).extract[InfoFile]
      log.info(s"parsed file $filePath")
      res
    }.toOption
  }
}

