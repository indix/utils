
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
  aggregate(productUtil)

lazy val productUtil = (project in file("util-product")).
  settings(
    name := "utils-product",
    libraryDependencies += (
        "org.scalatest" % "scalatest_2.11" % "2.2.3"
      )
  )
