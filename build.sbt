import sbt.Keys._

val libVersion = sys.env.get("TRAVIS_TAG") orElse sys.env.get("BUILD_LABEL") getOrElse s"1.0.0-${System.currentTimeMillis / 1000}-SNAPSHOT"

lazy val commonSettings = Seq(
  version := libVersion,
  autoAPIMappings := true,
  organization := "com.indix",
  organizationName := "Indix",
  organizationHomepage := Some(url("http://www.indix.com")),
  scalaVersion := "2.11.11",
  scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked"),
  javacOptions ++= Seq("-Xlint:deprecation", "-source", "1.6"),
  resolvers ++= Seq(
    "Clojars" at "http://clojars.org/repo",
    "Concurrent Maven Repo" at "http://conjars.org/repo",
    "Twttr Maven Repo" at "https://maven.twttr.com/"
  )
)

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  pgpSecretRing := file("local.secring.gpg"),
  pgpPublicRing := file("local.pubring.gpg"),
  pgpPassphrase := Some(sys.env.getOrElse("GPG_PASSPHRASE", "").toCharArray),
  credentials += Credentials("Sonatype Nexus Repository Manager",
   "oss.sonatype.org",
   System.getenv("SONATYPE_USERNAME"),
   System.getenv("SONATYPE_PASSWORD")),
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  pomExtra :=
    <url>https://github.com/indix/utils</url>
    <licenses>
      <license>
        <name>Apache License</name>
        <url>https://raw.githubusercontent.com/indix/utils/master/LICENSE</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:indix/utils.git</url>
      <connection>scm:git:git@github.com:indix/utils.git</connection>
    </scm>
    <developers>
      <developer>
        <id>indix</id>
        <name>Indix</name>
        <url>http://www.indix.com</url>
      </developer>
    </developers>
)

lazy val utils = (project in file(".")).
  settings(commonSettings: _*).
  settings(unidocSettings: _*).
  settings(site.settings ++ ghpages.settings: _*).
  settings(
    name := "utils",
    publish := { },
    site.addMappingsToSiteDir(mappings in(ScalaUnidoc, packageDoc), "latest/api"),
    git.remoteRepo := "git@github.com:indix/utils.git"
  ).
  aggregate(coreUtils, storeUtils, sparkUtils, gocdUtils)

lazy val coreUtils = (project in file("util-core")).
  settings(commonSettings: _*).
  settings(publishSettings: _*).
  settings(
    name := "util-core",
    crossScalaVersions := Seq("2.10.6", "2.11.11"),
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.3" % Test,
      "org.apache.commons" % "commons-lang3" % "3.5",
      "com.netaporter" %% "scala-uri" % "0.4.16"
    )
  )

lazy val storeUtils = (project in file("util-store")).
  settings(commonSettings: _*).
  settings(publishSettings: _*).
  settings(
    name := "util-store",
    crossScalaVersions := Seq("2.10.6", "2.11.11"),
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.3" % Test,
      "commons-io" % "commons-io" % "2.5",
      "com.twitter" %% "chill" % "0.8.1",
      "org.rocksdb" % "rocksdbjni" % "4.11.2"
    )
  )


lazy val sparkUtils = (project in file("util-spark")).
  settings(commonSettings: _*).
  settings(publishSettings: _*).
  settings(
    name := "util-spark",
    crossScalaVersions := Seq("2.10.6", "2.11.11"),
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.3" % Test,
      "org.apache.spark" %% "spark-core" % "2.2.0",
      "org.apache.spark" %% "spark-sql" % "2.2.0",
      "com.databricks" %% "spark-avro" % "4.0.0",
      "org.apache.hadoop" % "hadoop-aws" % "2.6.0" % Test,
      "com.indix" % "dfs-datastores" % "2.0.21" excludeAll(
        ExclusionRule(organization = "org.apache.hadoop"),
        ExclusionRule(organization = "org.eclipse.jetty")
        ),
      "org.apache.parquet" % "parquet-avro" % "1.8.1",
      "org.bdgenomics.utils" %% "utils-misc" % "0.2.13"
    )
  )

lazy val gocdUtils = (project in file("util-gocd")).
  settings(commonSettings: _*).
  settings(publishSettings: _*).
  settings(
    name := "util-gocd",
    crossScalaVersions := Seq("2.10.6", "2.11.11"),
    libraryDependencies ++= Seq(
      "org.apache.commons" % "commons-lang3" % "3.1",
      "commons-io" % "commons-io" % "1.3.2",
      "com.amazonaws" % "aws-java-sdk-s3" % "1.11.127",
      "cd.go.plugin" % "go-plugin-api" % "17.2.0" % Provided,
      "com.google.code.gson" % "gson" % "2.2.3",
      "junit" % "junit" % "4.12" % Test,
      "com.novocode" % "junit-interface" % "0.11" % Test,
      "org.mockito" % "mockito-all" % "1.10.19" % Test,
      "org.scalatest" %% "scalatest" % "3.0.3" % Test
    )
  )
