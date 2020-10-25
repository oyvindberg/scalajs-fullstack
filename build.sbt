import org.scalajs.linker.interface.ModuleKind.CommonJSModule

lazy val tutorial =
  crossProject(JSPlatform, JVMPlatform)
    .in(file("."))
    .settings(
      organization := "com.olvind",
      scalaVersion := "2.13.3",
      scalacOptions ++= Seq("-encoding", "UTF-8", "-feature", "-unchecked", "-Xlint", "-deprecation"),
      testFrameworks += new TestFramework("utest.runner.Framework"),
      /* shared dependencies */
      libraryDependencies ++= Seq(
        "com.lihaoyi" %%% "upickle" % "1.2.2",
        "com.lihaoyi" %%% "autowire" % "0.3.2",
        "com.lihaoyi" %%% "utest" % "0.7.5" % Test
      )
    )

lazy val tutorialJvm: Project =
  tutorial.jvm
    .enablePlugins(WebScalaJSBundlerPlugin)
    .settings(
      name := "tutorialJVM",
      /* Normal scala dependencies */
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-http" % "10.2.1",
        "com.typesafe.akka" %% "akka-stream" % "2.6.10",
      ),
      scalaJSProjects := Seq(tutorialJs),
      Assets / pipelineStages := Seq(scalaJSPipeline)
    )

lazy val tutorialJs: Project =
  tutorial.js
    .enablePlugins(ScalablyTypedConverterPlugin)
    .settings(
      name := "tutorialJS",
      webpackDevServerPort := 8081,
      /* discover main and make the bundle run it */
      scalaJSUseMainModuleInitializer := true,
      useYarn := true,
      /* disabled because it somehow triggers many warnings */
      scalaJSLinkerConfig := scalaJSLinkerConfig.value.withSourceMap(false).withModuleKind(CommonJSModule),
      /* scala.js dependencies */
      libraryDependencies ++= Seq(
        "me.shadaj" %% "slinky-core-ijext" % "0.6.6", // needs this for the intellij integration to work
      ),
      /* javascript dependencies */
      Compile / npmDependencies ++= Seq(
        "antd" -> "4.7.3",
        "react" -> "17.0.1",
        "react-dom" -> "17.0.1",
        "@types/react" -> "16.9.53",
        "@types/react-dom" -> "16.9.8",
      ),
      /* custom webpack file */
      Compile / webpackConfigFile := Some((ThisBuild / baseDirectory).value / "custom.webpack.config.js"),
      Compile / fastOptJS / webpackDevServerExtraArgs += "--mode=development",
      /* dependencies for custom webpack file */
      Compile / npmDevDependencies ++= Seq(
        "webpack-merge" -> "5.2.0",
        "css-loader" -> "5.0.0",
        "style-loader" -> "2.0.0",
        "file-loader" -> "6.1.1",
        "url-loader" -> "4.1.1",
        "html-webpack-plugin" -> "4.5.0",
      ),
      /* don't need to override anything for test. revisit this if you depend on code which imports resources,
          for instance (you probably shouldn't need to) */
      Test / webpackConfigFile := None,
      Test / npmDependencies ++= Seq(
        "source-map-support" -> "0.5.19"
      ),
      Test / requireJsDomEnv := true,
      stIgnore += "source-map-support",
      stFlavour := Flavour.Slinky,
      stReactEnableTreeShaking := Selection.All,
      scalacOptions += "-Ymacro-annotations",
    )

def cmd(name: String, commands: String*) =
  Command.command(name)(s => s.copy(remainingCommands = commands.toList.map(cmd => Exec(cmd, None)) ++ s.remainingCommands))

commands ++= List(
  cmd("dev", "fastOptJS::startWebpackDevServer", "~;tutorialJVM/reStart;tutorialJS/fastOptJS::webpack"),
  cmd("devFront", "fastOptJS::startWebpackDevServer", "~tutorialJS/fastOptJS::webpack"),
  cmd("devBack", "~;tutorialJVM/reStart"),
)
