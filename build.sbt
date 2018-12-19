name := "capi-wpi-querybot"

version := "1.0"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
// Dependencies needed to build as an AWS Lambda file
//  "com.amazonaws" % "aws-lambda-java-core" % "1.0.0",
//  "com.amazonaws" % "aws-lambda-java-events" % "1.0.0",
  "com.gu" %% "content-api-client-default" % "12.10",
  // Test dependencies
  "org.specs2" %% "specs2-core" % "4.3.5" % "test",
  //ok http dependencies
  "com.squareup.okhttp3" % "okhttp" % "3.12.0",
  //play json dependencies
  "com.typesafe.play" %% "play-json" % "2.6.10",
  //"com.typesafe.play" %% "play-functional" % "2.6.7",
  //play ws dependencies
  "com.typesafe.play" % "play-ws_2.12" % "2.6.0-M1",
//Taig communicator - wraps OkHttp fro Scala
  "io.taig" %% "communicator" % "3.5.1",
  //scalax.io
  "org.scalaforge" % "scalax" % "0.1",
  //aws S3 stuff
  "com.amazonaws" % "aws-java-sdk-s3" % "1.11.461",
  "com.typesafe" % "config" % "1.3.3",
  "org.scala-sbt" % "command_2.12" % "1.2.7",
  "org.scalactic" %% "scalactic" % "3.0.5",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  // gmail api dependencies
  "com.google.apis" % "google-api-services-gmail" % "v1-rev98-1.25.0",
  "javax.mail" % "mail" % "1.5.0-b01",
  "io.argonaut" %% "argonaut" % "6.2.2")

assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case x => MergeStrategy.first
}
