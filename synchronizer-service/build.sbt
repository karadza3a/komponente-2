name := """komponente2-synchronizer-service"""

version := "2.6.x"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.6"

crossScalaVersions := Seq("2.11.12", "2.12.4")

libraryDependencies += guice
libraryDependencies += ws

libraryDependencies += "com.univocity" % "univocity-parsers" % "2.6.1"
libraryDependencies += specs2 % Test
