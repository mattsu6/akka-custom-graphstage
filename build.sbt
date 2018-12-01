lazy val commonSettings = Seq(
  organization := "mattsu6",
  version := "0.0.1",
  scalaVersion := "2.12.6",
  scalacOptions in Compile ++= Seq(
    "-feature",
    "-deprecation",
    "-unchecked",
    "-encoding", "UTF-8",
    "-Xfatal-warnings",
    "-Xlint",
    "-Xfuture",
    "-language:existentials",
    "-language:implicitConversions",
    "-language:postfixOps",
    "-language:higherKinds",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused",
    "-Ywarn-unused-import",
    "-Ywarn-value-discard",
  ),
  scalacOptions in console -= "-Ywarn-unused-import",
  scalacOptions in Test ++= Seq("-Yrangepos"),
  scalafmtOnCompile in ThisBuild := true
)

lazy val root = (project in file("."))
  .settings(
    commonSettings,
    name := "akka-custom-graphstage",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-stream" % "2.5.18"
    )
  )
