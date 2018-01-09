name := "CourseWorkTrirdCourse"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies += "net.ruippeixotog" %% "scala-scraper" % "2.0.0"

resolvers += "Sonatype OSS Snapshots" at
  "https://oss.sonatype.org/content/repositories/releases"

libraryDependencies +=
  "com.storm-enroute" %% "scalameter-core" % "0.8.2"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

libraryDependencies += "io.monix" %% "monix" % "2.3.0"