import Resolvers._
import com.tapad.docker.DockerComposePlugin.autoImport.dockerImageCreationTask
import com.typesafe.sbt.SbtNativePackager.autoImport.executableScriptName
import com.typesafe.sbt.packager.Keys.stage
import sbt.Keys._
import sbt._
import sbtdocker.DockerPlugin.autoImport._

import scala.sys.process._

object Settings {

  //////////////////////////////////////////////////////////////////////////////
  // PROJECT INFO
  //////////////////////////////////////////////////////////////////////////////
  val ORG = "com.elevator.api"
  val PROJECT_NAME = "elevatorapi"
  val TEAM_NAME = "GauravSharma"
  val SCALA_VERSION_212 = "2.12.6"
  val SCALA_VERSION_211 = "2.11.12"
  val UnusedWarnings = "-Ywarn-unused:imports,privates,locals,implicits"
  val DOCKER_ORG = "elevator"

  val commonSettings = Seq(
    scalaVersion := SCALA_VERSION_212,
    organization := ORG,
    publishMavenStyle := true,
    resolvers ++= resolversDep,
    publishArtifact in Test := false,
    isSnapshot := true,
    scalacOptions ++= Seq("-encoding", "utf8"),
    scalacOptions ++= Seq(
      "-deprecation",
      "-explaintypes",
      "-feature",
      "-unchecked",
      "-language:dynamics",
      "-target:jvm-1.8",
      "-Xlint:-unused,_",
      "-Ypartial-unification",
      "-Ywarn-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-inaccessible",
      "-Ywarn-infer-any",
      "-Ywarn-value-discard",
      UnusedWarnings
    ),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full))

  lazy val buildNumber = SettingKey[Option[String]]("build-number", "The build number")
  lazy val user = TaskKey[Option[String]]("user", "The user")
  lazy val branch = TaskKey[Option[String]]("branch", "The Git branch")
  lazy val imageTags = TaskKey[Seq[String]]("image-tags", "The Docker image tags")
  val dockerRegistry = ""

  val jmxparam = "-Dcom.sun.management.jmxremote.port=9333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"
  val jvmparam = "-J-server -J-Xms1g -J-Xmx1g"
  val javaHeapParam = "-J-XX:+HeapDumpOnOutOfMemoryError -J-XX:HeapDumpPath=/var/log/$app_name/heapdump -J-XX:ErrorFile=/var/log/$app_name/hs_err_pid%p.log"

  lazy val dockerSettings = Seq(
    buildNumber := {
      Option(System.getProperty("build.number")).map(s ⇒ s.substring(0, s.lastIndexOf(".")))
    },
    branch := {
      Option(System.getProperty("teamcity.build.branch")).orElse {
        val command =
          s"git rev-parse --abbrev-ref HEAD"

        try {
          Option(command.!!.trim)
        } catch {
          case _: RuntimeException =>
            sLog.value.warn("Could not determine current branch, is Git installed?"); None
          case e: Throwable => throw e
        }
      }
    },
    user := {
      sys.env.get("USER").orElse(sys.env.get("USERNAME"))
    },
    imageTags in docker := {
      var imageTags: Seq[String] = Nil

      if (branch.value.getOrElse("!release") == "release") {
        imageTags = imageTags :+ "latest"
      } else {
        val tag = branch.value.orElse(Some(""))

        imageTags = imageTags :+
          s"${buildNumber.value.getOrElse((version in ThisBuild).value)}-" +
            tag
              .map("[^A-Za-z0-9]".r.replaceAllIn(_, "."))
              .getOrElse("snapshot")
      }
      imageTags
    },
    onLoadMessage := "Building API".stripMargin,
    docker := {
      println(
        s"""
           |==================================================================================
           |Setting version to the following for build:
           |${
          buildNumber.value.getOrElse(
            "<SNAPSHOT>")
        }
           |Branch used is:
           |${
          branch.value.
            getOrElse("<SNAPSHOT>")
        }
           |Docker image tags:
           |${(imageTags in docker).value.mkString(", ")}
           |==================================================================================""".stripMargin)
      docker.
        value
    },
    dockerfile in docker := {
      val appDir: File = stage.value
      /*val sqlDir: File = baseDirectory.value / "SQL"*/
      val app_name = "elevatorapi"
      val targetDir = s"/srv/${name.value}"
      println(s"##teamcity[setParameter name='DockerContainerCreated' value='$dockerRegistry/" +
        s"$DOCKER_ORG/$PROJECT_NAME:${(imageTags in docker).value.head}']")
      println(s"##teamcity[setParameter name='SbtDockerRegistry' value='$dockerRegistry']")
      new Dockerfile {
        from("")
        label("maintainer", DOCKER_ORG)
        user("root")
        env("http_proxy", s"")
        env("https_proxy", s"")
        runRaw(s"mkdir -p $targetDir")
        workDir(s"$targetDir")
        copy(appDir, targetDir)
        env("APP_NAME", s"$app_name")
        env("CONFIG_FILE", sys.env.getOrElse("CONFIG_FILE", "pinto.conf"))
        env("LOG_CONFIG_FILE", "logback.xml")
        env("LOG_DIR", s"/var/log/$app_name")
        expose(8088)
        entryPointRaw(
          s"sleep 10;" +
          /*  s"$targetDir/SQL/sync.sh;" +*/
            s"$targetDir/bin/${executableScriptName.value}" +
            s" -Dappname=$${APP_NAME}" +
            s" -Dconfig.file=$targetDir/conf/$${CONFIG_FILE}" +
            s" -Ddockerized_app=true" +
            s" -Dlog.dir=$${LOG_DIR}" +
            s" -Dlogback.configurationFile=$targetDir/conf/$${LOG_CONFIG_FILE}" +
            s" -Dapp_as_service=true" +
            s" $jvmparam $jmxparam $javaHeapParam && echo")
      }
    },
    imageNames in docker := Seq(
      ImageName(s"$ORG/${name.value}:latest")),
    dockerImageCreationTask := docker.value,
    buildOptions in docker := BuildOptions(
      cache = true,
      removeIntermediateContainers = BuildOptions.Remove.Always,
      pullBaseImage = BuildOptions.Pull.IfMissing))

}
