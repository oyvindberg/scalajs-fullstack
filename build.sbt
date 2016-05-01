import org.scalajs.sbtplugin.cross.CrossProject

enablePlugins(ScalaJSPlugin)

lazy val commonSettings: Seq[Def.Setting[_]] =
  Seq(
    organization         := "com.olvind",
    scalaVersion         := "2.11.8",
    scalacOptions       ++= Seq("-encoding", "UTF-8", "-feature", "-unchecked", "-Xlint", "-Yno-adapted-args", "-Xfuture", "-deprecation")
  )

lazy val tutorial: CrossProject =
  crossProject.in(file("."))
  .settings(commonSettings :_*)
  .settings(
    name := "scala-js-workshop",
    /* shared dependencies */
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle"   % "0.4.0",
      "com.lihaoyi" %%% "autowire"  % "0.2.5",
      "com.lihaoyi" %%% "scalatags" % "0.5.4",
      "com.lihaoyi" %%% "utest"     % "0.3.0" % Test
    )
  ).jvmSettings(
    /* Normal scala dependencies */
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http-core"         % "2.4.4",
      "com.typesafe.akka" %% "akka-http-experimental" % "2.4.4",
      /* we include this because the server serves CSS from the classpath. Not a good solution! */
      WebJars.bootstrap
    )
  ).jsSettings(
    /* scala.js dependencies */
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom"    % "0.9.0",
      "be.doeraene"  %%% "scalajs-jquery" % "0.9.0"
    ),

    /* javascript dependencies */
    skip in packageJSDependencies := false,
    jsDependencies ++=
      Seq(
        /* have to specify which javascript file within the jar we want to include */
        WebJars.jquery    / "2.1.4/jquery.js",
        WebJars.bootstrap / "3.3.5/js/bootstrap.js" minified "3.3.5/js/bootstrap.min.js",
        RuntimeDOM
    ),

    /* uTest settings*/
    testFrameworks += new TestFramework("utest.runner.Framework"),

    /* generate javascript launcher */
    persistLauncher in Compile := true,
    persistLauncher in Test := false,

    /* for workbench */
    bootSnippet := "tutorial.App().main();"

).jsSettings(workbenchSettings :_*)

lazy val tutorialJvm: Project =
  tutorial.jvm

lazy val tutorialJs: Project =
  tutorial.js

lazy val root: Project =
  project.in(file("."))
  .aggregate(tutorialJs, tutorialJvm)
  .settings(commonSettings :_*)
  .settings(
    publish := {},
    publishLocal := {}
  )
