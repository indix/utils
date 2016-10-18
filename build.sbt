
val libVersion = sys.env.getOrElse("SNAP_PIPELINE_COUNTER", "0.1.0-SNAPSHOT")


lazy val commonSettings = Seq(
  organization := "com.indix",
  version := libVersion,
  autoAPIMappings := true,
  organizationName := "Indix",
  organizationHomepage := Some(url("http://oss.indix.com")),
  scalaVersion := "2.11.8",
  scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked"),
  javacOptions ++= Seq("-Xlint:deprecation", "-source", "1.7"),
  resolvers ++= Seq(
    "Clojars" at "http://clojars.org/repo",
    "Concurrent Maven Repo" at "http://conjars.org/repo",
    "Twttr Maven Repo"  at "http://maven.twttr.com/"
  )
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
  aggregate(coreUtils, sparkUtils)

lazy val coreUtils = (project in file("util-core")).
  settings(commonSettings: _*).
  settings(publishSettings: _*).
  settings(
    name := "util-core",
    crossScalaVersions := Seq("2.10.6", "2.11.8"),
    libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % "2.2.6",
        "org.apache.commons" % "commons-lang3" % "3.4"
      )
  )


lazy val sparkUtils = (project in file("util-spark")).
  settings(commonSettings: _*).
  settings(publishSettings: _*).
  settings(
    name := "util-spark",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "2.2.6",
      "org.apache.spark" %% "spark-core" % "2.0.0",
      "org.apache.spark" %% "spark-sql" % "2.0.0",
      "com.databricks"   %% "spark-avro" % "3.0.1",
      "com.indix"  % "dfs-datastores" % "2.0.13" excludeAll(
        ExclusionRule(organization = "org.apache.hadoop"), ExclusionRule(organization = "org.eclipse.jetty")
      ),
      "com.twitter" % "parquet-avro" % "1.6.0",
      "org.bdgenomics.utils" %% "utils-misc" % "0.2.2"
    )
  )
