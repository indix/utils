
lazy val commonSettings = Seq(
  organization := "com.indix",
  version := "0.1.0",
  scalaVersion := "2.11.7",
  autoAPIMappings := true
)

lazy val root = (project in file(".")).
  settings(commonSettings: _* ).
  settings(unidocSettings: _*).
  settings(site.settings ++ ghpages.settings: _*).
  settings(
    name := "utils",
    site.addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc), "latest/api"),
    git.remoteRepo := "git@github.com:ind9/utils.git"
  ).
  aggregate(coreUtils)

lazy val coreUtils = (project in file("util-core")).
  settings(
    name := "utils-product",
    libraryDependencies += (
        "org.scalatest" %% "scalatest" % "2.2.3"
      )
  )
