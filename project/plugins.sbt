addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.8")
addSbtPlugin("io.spray" % "sbt-revolver" % "0.8.0")

resolvers += Resolver.url("olvindberg-sbt-plugins",
  url("https://dl.bintray.com/olvindberg/sbt-plugins/"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.olvind" % "workbench" % "0.2.6")