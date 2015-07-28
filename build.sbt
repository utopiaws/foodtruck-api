import NativePackagerKeys._
import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.archetypes.ServerLoader
import sbt.Keys._

seq(packagerSettings:_*)

packageArchetype.java_server

serverLoading in Debian := ServerLoader.Upstart

lazy val root = (project in file(".")).enablePlugins(PlayScala)

name := "foodtruck-api"

name in Debian := "foodtruck-api"

defaultLinuxInstallLocation := "/mnt"

defaultLinuxLogsLocation := "/mnt/logs"

val releaseVersion = "0.1.0"

val gitSha = ("git rev-parse HEAD" !!).trim

version := releaseVersion + "-" + gitSha

packageSummary := "Scala version of foodtruck-API server"

packageDescription := "Scala version of fooktruck-API server"

debianPackageDependencies in Debian ++= Seq("java6-runtime")


libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "com.google.guava" % "guava" % "18.0",
  "com.google.code.findbugs" % "jsr305" % "3.0.0"
)

// test dependencies
libraryDependencies ++= Seq(
  "org.scalatestplus" %% "play" % "1.1.0" % "test"
)

// For code coverage
instrumentSettings
