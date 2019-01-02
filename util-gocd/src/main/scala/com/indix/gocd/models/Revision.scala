package com.indix.gocd.models

/**
  * Represents revision
  * Parses revision string to populate fields
  *
  * @param revision
  */
case class Revision(revision: String) extends Ordered[Revision] {

  val parts: Seq[String] = revision.split("\\.")
  val major: Int = Integer.valueOf(parts.head)
  val minor: Int = Integer.valueOf(parts(1))
  val patch: Int = {
    if (parts.size == 3) Integer.valueOf(parts(2))
    else 0
  }

  override def compare(that: Revision): Int = {
  val majorDiff = this.major.compareTo(that.major)
      val minorDiff = this.minor.compareTo(that.minor)
      val patchDiff = this.patch.compareTo(that.patch)

      if (majorDiff != 0) majorDiff
      else if (minorDiff != 0) minorDiff
      else if (patchDiff != 0) patchDiff
      else 0
  }
}

object Revision {
  def base: Revision = Revision("0.0.0")
}
