enablePlugins(ScalaJSPlugin)

lazy val common: Seq[Def.Setting[_]] =
  Seq(
    organization         := "com.olvind",
    scalaVersion         := "2.11.8",
    scalacOptions       ++= Seq("-encoding", "UTF-8", "-feature", "-language:existentials", "-language:higherKinds", "-language:implicitConversions", "-unchecked", "-Xlint", "-Yno-adapted-args", "-Ywarn-dead-code", "-Ywarn-numeric-widen", "-Ywarn-value-discard", "-Xfuture", "-deprecation")
  )

lazy val tutorial = crossProject.in(file("."))
  .settings(common :_*)
  .settings(
    name := "Scala.js Tutorial"
  ).jvmSettings(
    /* Add JVM-specific settings here */

  ).jsSettings(
    /* scala dependencies */
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom"    % "0.9.0",
      "be.doeraene"  %%% "scalajs-jquery" % "0.9.0",
      "com.lihaoyi"  %%% "utest"          % "0.3.0" % Test
    ),

    /* javascript dependencies */
    skip in packageJSDependencies := false,
    jsDependencies ++=
      Seq(
        "org.webjars" % "jquery" % "2.1.4" / "2.1.4/jquery.js",
        RuntimeDOM
    ),

    /* uTest settings*/
    testFrameworks += new TestFramework("utest.runner.Framework"),

    /* generate javascript launcher */
    persistLauncher in Compile := true,
    persistLauncher in Test := false
)

lazy val tutorialJvm = tutorial.jvm
lazy val tutorialJs = tutorial.js

lazy val root = project.in(file("."))
  .aggregate(tutorialJs, tutorialJvm)
  .settings(common :_*)
  .settings(
    publish := {},
    publishLocal := {}
  )
