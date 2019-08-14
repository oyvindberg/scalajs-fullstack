// shadow sbt-scalajs' crossProject and CrossType from Scala.js 0.6.x
import sbtcrossproject.CrossPlugin.autoImport.crossProject

lazy val tutorial =
  crossProject(JSPlatform, JVMPlatform)
    .in(file("."))
    .configure(baseSettings)
    .settings(
      /* shared dependencies */
      libraryDependencies ++= Seq(
        "com.github.japgolly.scalacss" %%% "core" % "0.5.6",
        "com.github.japgolly.scalacss" %%% "ext-scalatags" % "0.5.6",
        "com.lihaoyi" %%% "upickle" % "0.7.5",
        "com.lihaoyi" %%% "autowire" % "0.2.6",
        "com.lihaoyi" %%% "scalatags" % "0.7.0",
        "com.lihaoyi" %%% "utest" % "0.7.1" % Test
      )
    )

lazy val tutorialJvm: Project =
  tutorial.jvm
    .enablePlugins(WebScalaJSBundlerPlugin)
    .settings(
      name := "tutorialJVM",
      /* Normal scala dependencies */
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-http" % "10.1.9",
        "com.typesafe.akka" %% "akka-stream" % "2.5.23",
      ),
      scalaJSProjects := Seq(tutorialJs),
      Assets / pipelineStages := Seq(scalaJSPipeline)
    )

lazy val tutorialJs: Project =
  tutorial.js
    .enablePlugins(ScalaJSBundlerPlugin)
    .settings(
      name := "tutorialJS",
      webpackDevServerPort := 8081,
      /* discover main and make the bundle run it */
      scalaJSUseMainModuleInitializer := true,
      useYarn := true,
      /* disabled because it somehow triggers many warnings */
      Compile / emitSourceMaps := false,
      /* in preparation for scala.js 1.0 */
      scalacOptions += "-P:scalajs:sjsDefinedByDefault",
      /* scala.js dependencies */
      libraryDependencies ++= Seq(
        "org.scala-js" %%% "scalajs-dom" % "0.9.7",
        "org.scala-js" %%% "scalajs-java-time" % "0.2.5"
      ),
      /* javascript dependencies */
      Compile / npmDependencies ++= Seq(
        "bootstrap" -> "4.3.1",
        "jquery" -> "3.4.1",
      ),
      /* custom webpack file */
      Compile / webpackConfigFile := Some((ThisBuild / baseDirectory).value / "custom.webpack.config.js"),
      /* dependencies for custom webpack file */
      Compile / npmDevDependencies ++= Seq(
        "webpack-merge" -> "4.1",
        "css-loader" -> "2.1.0",
        "style-loader" -> "0.23.1",
        "file-loader" -> "3.0.1",
        "url-loader" -> "1.1.2",
        "html-webpack-plugin" -> "3.2.0",
      ),
      /* don't need to override anything for test. revisit this if you depend on code which imports resources,
          for instance (you probably shouldn't need to) */
      Test / webpackConfigFile := None,
      Test / npmDependencies ++= Seq(
        "source-map-support" -> "0.5.13"
      ),
      Test / requireJsDomEnv := true,
    )

lazy val baseSettings: Project => Project =
  _.settings(
    organization := "com.olvind",
    scalaVersion := "2.12.9",
    scalacOptions ++= Seq("-encoding", "UTF-8", "-feature", "-unchecked", "-Xlint", "-Yno-adapted-args", "-Xfuture", "-deprecation"),
    testFrameworks += new TestFramework("utest.runner.Framework"),
  )

def cmd(name: String, commands: String*) =
  Command.command(name)(s => s.copy(remainingCommands = commands.toList.map(cmd => Exec(cmd, None)) ++ s.remainingCommands))

commands ++= List(
  cmd("dev", "fastOptJS::startWebpackDevServer", "~;tutorialJVM/reStart;tutorialJS/fastOptJS::webpack"),
  cmd("devFront", "fastOptJS::startWebpackDevServer", "~tutorialJS/fastOptJS::webpack"),
  cmd("devBack", "~;tutorialJVM/reStart"),
)
