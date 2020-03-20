import sbt._

object Resolvers {
  val resolversDep = Seq(
    "Typesafe Releases1" at "http://repo.typesafe.com/typesafe/maven-releases/",
    "Typesafe Releases2" at "http://repo.typesafe.com/typesafe/releases/",
    "OSS Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
    Resolver.mavenCentral,
    Resolver.typesafeIvyRepo("releases"),
    Resolver.sbtPluginRepo("releases"),
    Resolver.sbtIvyRepo("releases"),
    Resolver.sonatypeRepo("releases"),
    Resolver.bintrayRepo("hseeberger", "maven"),
    Classpaths.typesafeReleases,
    "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
    Classpaths.sbtPluginReleases)
}
