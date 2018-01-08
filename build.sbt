name := "CourseWorkTrirdCourse"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies += "net.ruippeixotog" %% "scala-scraper" % "2.0.0"

resolvers += "Sonatype OSS Snapshots" at
  "https://oss.sonatype.org/content/repositories/releases"

libraryDependencies +=
  "com.storm-enroute" %% "scalameter-core" % "0.8.2"

libraryDependencies += "io.monix" %% "monix" % "2.3.0"

resolvers ++= Seq(Resolver.sbtPluginRepo("releases"), Resolver.sbtPluginRepo("snapshots"))

libraryDependencies ++= Seq(
  // notice the double %% here
  "com.eed3si9n" %% "sbt-assembly" % "0.13.0"
)