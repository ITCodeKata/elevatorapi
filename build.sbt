import Dependencies._
import Settings._
import com.tapad.docker.DockerComposePlugin
import com.typesafe.sbt.packager.archetypes.JavaServerAppPackaging
import sbt.Keys._
import sbt._
import scoverage.ScoverageKeys._

import scala.sys.process.Process
import scala.util.Try

lazy val `common` = (project in file("common"))
  .settings(commonSettings: _*)
  .settings(
    name := "common",
    libraryDependencies ++= commonDeps,
    crossScalaVersions := Seq(SCALA_VERSION_211, SCALA_VERSION_212)
  )
  .dependsOn(`shared-model`)

lazy val `shared-model` = (project in file("shared-model"))
  .settings(commonSettings: _*)
  .settings(
    name := "share-model",
    libraryDependencies ++= sharedModelDeps,
    crossScalaVersions := Seq(SCALA_VERSION_211, SCALA_VERSION_212)
  )

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    name := PROJECT_NAME,
    libraryDependencies ++= rootProjectDeps,
    aggregate in reStart := false,
    aggregate in run := false,
    crossScalaVersions := Nil,
    excludeDependencies += "org.slf4j" % "slf4j-simple",
  )
  .settings(dockerSettings: _*)
  .dependsOn(`shared-model`, `common` % "test->test;compile->compile")
  .enablePlugins(BuildInfoPlugin, DockerComposePlugin, JavaServerAppPackaging, sbtdocker.DockerPlugin)

//--- Build Info ---//
buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion,
  "builtAt" -> {
    val dtf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    dtf.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Bangkok"))
    dtf.format(new java.util.Date())
  },
  "builtAtMillis" -> System.currentTimeMillis(),
  "gitHash" -> Try(Process("git rev-parse HEAD").!!.filter(_ >= ' ')).toOption.getOrElse("unknown"),
  "gitLog" -> Try(Process("./gitlog.sh").!!.filter(_ >= ' ')).getOrElse(Try(Process("gitlog.bat").!!.filter(_ >= ' ')).getOrElse("unknown"))
)

//Coverage setup

coverageEnabled in Test := true
coverageMinimum := 50
coverageFailOnMinimum := true

mappings in Universal ++= {
  ((sourceDirectory in Compile).value / "resources" * "*").get.map( f ⇒ f -> s"conf/${f.name}")
}

/*mappings in Universal ++= {
  ((sourceDirectory in Compile).value / "sql" * "*").get.map( f ⇒ f -> s"sql/${f.name}" )
}*/

/** New CSpider **/
/*mappings in Universal ++= {
  ((baseDirectory in Compile).value / "puppet/modules/puppets/manifests" * "*").get.map { f =>
    f -> s"puppet/modules/puppets/manifests/${f.name}"
  }
}*/

parallelExecution in Test := false

javaOptions in Universal ++= Seq("-jvm-debug 5005")

addCommandAlias("rebuild", ";clean; compile; package")
addCommandAlias("package", "test:package")