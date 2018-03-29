package packageName

import java.io.InputStream

final case class Post (url: String, texts: List[String], images: List[InputStream])
