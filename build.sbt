name := """ToDoAPI"""
organization := "com.brian"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.6"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
// https://mvnrepository.com/artifact/com.typesafe.play/play-json-joda
libraryDependencies += "com.typesafe.play" %% "play-json-joda" % "2.7.4"




// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.brian.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.brian.binders._"
