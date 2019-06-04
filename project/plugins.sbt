logLevel := Level.Error

resolvers += "Typesafe repository" at "https://dl.bintray.com/typesafe/maven-releases/"

addSbtPlugin("com.gu" % "sbt-riffraff-artifact" % "1.1.9")

libraryDependencies += "org.vafer" % "jdeb" % "1.3" artifacts (Artifact("jdeb", "jar", "jar"))