
lazy val commonSettings = Seq(
  organization := "com.indix",
  version := "0.1.0",
  scalaVersion := "2.11.7"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _* ).
  aggregate(productUtil)

lazy val productUtil = (project in file("util-product")).
  settings(
    name := "utils-product",
    libraryDependencies += (
        "org.scalatest" % "scalatest_2.11" % "2.2.3"
      )
  ).enablePlugins ( SiteScaladocPlugin )


ghpages.settings

git.remoteRepo := "git@github.com:ind9/utils.git"

site.includeScaladoc()