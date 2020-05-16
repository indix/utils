package com.indix.gocd.utils

import java.nio.file.Paths

import com.thoughtworks.go.plugin.api.task.JobConsoleLogger

import scala.collection.JavaConverters._

case class Context(environmentVariables: Map[String, String], workingDir: String, console: JobConsoleLogger) {

  def printMessage(message: String): Unit = console.printLine(message)

  def printEnvironment(): Unit = console.printEnvironment(environmentVariables.asJava)

  def getAbsoluteWorkingDir: String = Paths.get("").toAbsolutePath.resolve(workingDir).toString
}

object Context {
  def apply(context: Map[String, _]): Context = Context(context("environmentVariables").asInstanceOf[Map[String, String]], context("workingDirectory").asInstanceOf[String], JobConsoleLogger.getConsoleLogger)
}
