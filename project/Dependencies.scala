/**
  * Created by ksukhakanlan on 7/12/2017 AD.
  */
import sbt.Keys.libraryDependencies
import sbt._

object Dependencies {

  val akkaHttpCircleVersion = "1.21.1"
  val akkaHttpVersion = "10.1.5"
  val akkaStreamVersion = "2.5.23"
  val catsCoreVersion = "1.5.0"
  val catsEffectVersion = "1.1.0"
  val circeVersion = "0.9.3"
  val cucumberVersion = "4.7.0"
  val ficusVersion                     = "1.4.3"
  val logbackVersion                   = "1.2.3"
  val macwireVersion                   = "2.3.0"
  val mssqlJdbcVersion                 = "7.2.2.jre8"
  val scalaJVersion                    = "2.4.2"
  val softwareMillVersion              = "1.4.2"
  val specs2Version                    = "3.8.9"
  val swaggerUiVersion                 = "3.20.1"
  val swaggerAkkaHttpVersion           = "1.0.0"
  val typesafeConfigVersion            = "1.3.2"
  val scalaLoggingVersion              = "3.9.0"
  val qosLogbackVersion                = "1.2.3"
  val commonsCodecVersion              = "1.9"
  val plexusUtilsVersion               = "3.2.0"
  val scalacacheVersion                = "0.9.3"
  val chimneyAutoMapperVersion         = "0.3.2"
  val akkaHttpCacheVersion             = "10.1.8"
  val richMoneyVersion                 = "0.13.0"
  val wireMockVersion                  = "2.23.2"
  val jacksonModuleVersion             = "2.9.9"

  lazy val jacksonModuleDep = Seq(
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonModuleVersion
  )

  lazy val catsDep = Seq(
    "org.typelevel" %% "cats-core"   % catsCoreVersion,
    "org.typelevel" %% "cats-effect" % catsEffectVersion
  )

  lazy val scalaJDeps = Seq(
    "org.scalaj" %% "scalaj-http" % scalaJVersion
  )

  lazy val sttpDeps = Seq(
    "com.softwaremill.sttp" %% "circe" % softwareMillVersion
  )

  lazy val testDeps = Seq(
    "org.specs2"    %% "specs2-core"   % specs2Version % Test,
    "org.specs2"    %% "specs2-mock"   % specs2Version % Test,
    "org.scalatest" % "scalatest_2.12" % "3.0.5"       % Test,
    "org.mockito"   % "mockito-all"    % "1.9.5"       % Test
  )

  lazy val ficusDeps = Seq("com.iheart" %% "ficus" % ficusVersion)

  lazy val consulServiceDeps = Seq(
    "com.typesafe" % "config" % typesafeConfigVersion
  ) ++ ficusDeps ++ testDeps

  lazy val `(de)serializerDeps` = Seq(
    "io.circe"          %% "circe-core"           % circeVersion,
    "io.circe"          %% "circe-generic"        % circeVersion,
    "io.circe"          %% "circe-parser"         % circeVersion,
    "io.circe"          %% "circe-generic-extras" % circeVersion,
    "de.heikoseeberger" %% "akka-http-circe"      % akkaHttpCircleVersion
  )

  lazy val diDeps = Seq(
    "com.softwaremill.macwire" %% "macros"     % macwireVersion % "provided",
    "com.softwaremill.macwire" %% "macrosakka" % macwireVersion % "provided",
    "com.softwaremill.macwire" %% "util"       % macwireVersion
  )

  lazy val akkaDeps = Seq(
    "com.typesafe.akka" %% "akka-http"           % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-testkit"   % akkaHttpVersion % Test,
    "com.typesafe.akka" %% "akka-stream"         % akkaStreamVersion,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaStreamVersion % Test,
    "com.typesafe.akka" %% "akka-slf4j"          % akkaStreamVersion,
    "ch.qos.logback"    % "logback-classic"      % logbackVersion
  )

  val swaggerDeps = Seq(
    "org.webjars"                  % "swagger-ui"         % swaggerUiVersion,
    "com.github.swagger-akka-http" %% "swagger-akka-http" % swaggerAkkaHttpVersion excludeAll (
      ExclusionRule(organization = "org.reflections", name = "reflections"),
      ExclusionRule(organization = "com.typesafe.akka")
    )
  )

  val loggingDeps = Seq(
    "com.typesafe.scala-logging" %% "scala-logging"  % scalaLoggingVersion,
    "ch.qos.logback"             % "logback-classic" % qosLogbackVersion
  )

  val autoMapperDeps = Seq(
    "io.scalaland" %% "chimney" % chimneyAutoMapperVersion
  )

  val cacheDeps = Seq(
    "com.github.cb372" %% "scalacache-guava" % scalacacheVersion
  )

  val lruCacheDeps = Seq(
    "com.typesafe.akka" %% "akka-http-caching" % akkaHttpCacheVersion
  )

  lazy val commonDeps = Seq(
    "commons-codec"            % "commons-codec"    % commonsCodecVersion,
    "org.codehaus.plexus"      % "plexus-utils"     % plexusUtilsVersion,
    "com.github.nscala-money"  %% "nscala-money"    % richMoneyVersion
  ) ++
    consulServiceDeps ++
    akkaDeps ++
    diDeps ++
    `(de)serializerDeps` ++
    loggingDeps ++
    catsDep ++
    cacheDeps

  lazy val sharedModelDeps =
    `(de)serializerDeps` ++
      swaggerDeps ++
      testDeps ++
      autoMapperDeps

  lazy val rootProjectDeps =
    loggingDeps ++
      `(de)serializerDeps` ++
      consulServiceDeps ++
      diDeps ++
      akkaDeps ++
      catsDep ++
      swaggerDeps ++
      testDeps ++
      ficusDeps ++
      lruCacheDeps ++
      Seq("org.scalaj" %% "scalaj-http" % "2.3.0")
}
