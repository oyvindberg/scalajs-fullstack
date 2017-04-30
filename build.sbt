import org.scalajs.sbtplugin.cross.CrossProject

lazy val commonSettings: Seq[Def.Setting[_]] =
  Seq(
    organization := "com.olvind",
    scalaVersion := "2.12.2",
    scalacOptions ++= Seq("-encoding",
                          "UTF-8",
                          "-feature",
                          "-unchecked",
                          "-Xlint",
                          "-Yno-adapted-args",
                          "-Xfuture",
                          "-deprecation")
  )

lazy val tutorial: CrossProject =
  crossProject
    .in(file("."))
    .settings(commonSettings: _*)
    .settings(
      name := "scala-js-workshop",
      /* shared dependencies */
      libraryDependencies ++= Seq(
        "com.github.japgolly.scalacss" %%% "core"          % "0.5.3",
        "com.github.japgolly.scalacss" %%% "ext-scalatags" % "0.5.3",
        "com.lihaoyi"                  %%% "upickle"       % "0.4.4",
        "com.lihaoyi"                  %%% "autowire"      % "0.2.6",
        "com.lihaoyi"                  %%% "scalatags"     % "0.6.5",
        "com.lihaoyi"                  %%% "utest"         % "0.4.5" % Test
      )
    )
    .jvmSettings(
      /* Normal scala dependencies */
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-http" % "10.0.5",
        /* we include this because the server serves CSS from the classpath. Not a good solution! */
        WebJars.bootstrap
      ),
      WebKeys.packagePrefix in Assets := "public/",
      managedClasspath in Runtime += (packageBin in Assets).value,
      pipelineStages in Assets := Seq(scalaJSPipeline),
      scalaJSProjects := Seq(tutorialJs)
    )
    .jvmConfigure(_.enablePlugins(SbtWeb))
    .jsConfigure(_ enablePlugins ScalaJSWeb)
    .jsSettings(
      /* scala.js dependencies */
      libraryDependencies ++= Seq(
        "org.scala-js" %%% "scalajs-dom" % "0.9.1"
      ),
      /* javascript dependencies */
      skip in packageJSDependencies := false,
      jsDependencies ++=
        Seq(
          /* have to specify which javascript file within the jar we want to include since there are several */
          WebJars.jquery / "2.1.4/jquery.js",
          WebJars.bootstrap / "3.3.5/js/bootstrap.js" minified "3.3.5/js/bootstrap.min.js",
          RuntimeDOM
        ),
      /* uTest settings*/
      testFrameworks += new TestFramework("utest.runner.Framework"),
      /* generate javascript launcher */
      scalaJSUseMainModuleInitializer := true,

      /* change so both fastOptJS and fullOptJS artifacts have same name */
      artifactPath in (Compile, fastOptJS) :=
        ((crossTarget in (Compile, fastOptJS)).value / ((moduleName in fastOptJS).value + "-opt.js"))
    )

lazy val tutorialJvm: Project =
  tutorial.jvm

lazy val tutorialJs: Project =
  tutorial.js.enablePlugins(WorkbenchPlugin)

def addCommandAliases(m: (String, String)*): Project => Project =
  _.settings(m.map(p => addCommandAlias(p._1, p._2)).reduce(_ ++ _): _*)

lazy val root: Project =
  project
    .in(file("."))
    .aggregate(tutorialJs, tutorialJvm)
    .settings(commonSettings: _*)
    .settings(
      name := "scalajs-workshop-root",
      publish := {},
      publishLocal := {}
    )
    .configure(addCommandAliases(
      "dev"      -> "~;tutorialJVM/re-start;tutorialJS/fastOptJS;refreshBrowsers",
      "devFront" -> "~;tutorialJS/fastOptJS;refreshBrowsers",
      "devBack"  -> "~;tutorialJVM/re-start"
    ))
