import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.11.11",
      version      := "1.0.0"
    )),
    name := "Salesforce",
    libraryDependencies += scalaTest % Test,
    libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.5.1",
    libraryDependencies += "com.typesafe" % "config" % "1.3.1",
    libraryDependencies += "com.google.code.gson" % "gson" % "2.8.2"
  )

