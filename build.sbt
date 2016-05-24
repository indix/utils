
val libVersion = sys.env.getOrElse("SNAP_PIPELINE_COUNTER", "0.1.0-SNAPSHOT")


lazy val commonSettings = Seq(
  organization := "com.indix",
  version := libVersion,
  scalaVersion := "2.11.7",
  autoAPIMappings := true,
  organizationName := "Indix",
  organizationHomepage := Some(url("http://oss.indix.com")),
  scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked"),
  javacOptions ++= Seq("-Xlint:deprecation", "-source", "1.7")
)

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  pomExtra := <url>https://github.com/ind9/utils</url>
    <licenses>
      <license>
        <name>Apache License</name>
        <url>https://raw.githubusercontent.com/ind9/utils/master/LICENSE</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:ind/utils.git</url>
      <connection>scm:git:git@github.com:ind9/utils.git</connection>
    </scm>
    <developers>
      <developer>
        <id>indix</id>
        <name>Indix</name>
        <url>http://oss.indix.com</url>
      </developer>
    </developers>
)

lazy val root = (project in file(".")).
  settings(commonSettings: _* ).
  settings(unidocSettings: _*).
  settings(publishSettings: _*).
  settings(site.settings ++ ghpages.settings: _*).
  settings(
    name := "utils",
    site.addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc), "latest/api"),
    git.remoteRepo := "git@github.com:ind9/utils.git"
  ).
  aggregate(coreUtils)

lazy val coreUtils = (project in file("util-core")).
  settings(commonSettings: _*).
  settings(publishSettings: _*).
  settings(
    name := "util-core",
    libraryDependencies += (
      "org.scalatest" %% "scalatest" % "2.2.3"
      )
  )
