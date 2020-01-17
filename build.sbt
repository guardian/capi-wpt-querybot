name := "capi-wpt-querybot"

version := "1.0"

scalaVersion := "2.12.8"

lazy val awsVersion = "1.11.708"

libraryDependencies ++= Seq(
  "com.gu" %% "content-api-client-default" % "12.10",
  // Test dependencies
  "org.specs2" %% "specs2-core" % "4.3.5" % "test",
  //ok http dependencies
  "com.squareup.okhttp3" % "okhttp" % "3.12.0",
  //play json dependencies
//  "com.typesafe.play" %% "play-json" % "2.6.10",
  //"com.typesafe.play" %% "play-functional" % "2.6.7",
  //play ws dependencies
//  "com.typesafe.play" % "play-ws_2.12" % "2.6.0-M1",
//Taig communicator - wraps OkHttp fro Scala
  "io.taig" %% "communicator" % "3.5.1",
  //scalax.io
  "org.scalaforge" % "scalax" % "0.1",
  //aws S3 stuff
  "com.amazonaws" % "aws-java-sdk-s3" % awsVersion,
  "com.typesafe" % "config" % "1.3.3",
  "org.scala-sbt" % "command_2.12" % "1.2.7",
  "org.scalactic" %% "scalactic" % "3.0.5",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  // gmail api dependencies
  "com.google.apis" % "google-api-services-gmail" % "v1-rev98-1.25.0",
  "javax.mail" % "mail" % "1.5.0-b01",
  "io.argonaut" %% "argonaut" % "6.2.2",
  "com.gu" % "kinesis-logback-appender" % "1.4.4",
  "com.amazonaws" % "aws-java-sdk-ec2" % awsVersion,
  "net.logstash.logback" % "logstash-logback-encoder" % "4.2",
  ws
)

routesGenerator := InjectedRoutesGenerator

lazy val root = (project in file(".")).enablePlugins(PlayScala, RiffRaffArtifact, JDebPackaging, SystemdPlugin)
  .settings(Defaults.coreDefaultSettings: _*)
  .settings(
    name in Universal := normalizedName.value,
    topLevelDirectory := Some(normalizedName.value),
    riffRaffPackageName := name.value,
    riffRaffManifestProjectName := s"dotcom:${name.value}",
    riffRaffUploadArtifactBucket := Option("riffraff-artifact"),
    riffRaffUploadManifestBucket := Option("riffraff-builds"),

    riffRaffPackageType := (packageBin in Debian).value,

    debianPackageDependencies := Seq("openjdk-8-jre-headless"),
    maintainer := "Dotcom <dotcom.platform@guardian.co.uk>",
    packageSummary := "Web page test bot",
    packageDescription := "Page weight checker that uses web page test",
    mainClass := Some("app.App"),

//    riffRaffArtifactResources ++= Seq(
//      baseDirectory.value / "cloudformation" / "Transcribe.yml" -> s"packages/cloudformation/Transcribe.yml"
//    ),
    javaOptions in Universal ++= Seq(
      "-Dpidfile.path=/dev/null",
      "-J-Xmx8G",
      "-J-Xss2M"
    )
  )

