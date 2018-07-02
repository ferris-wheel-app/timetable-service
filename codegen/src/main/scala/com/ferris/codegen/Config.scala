package com.ferris.codegen

object Config{
  val scriptDir = "src/main/resources/db/migration/"
  val initScripts = FileUtils.listFiles(scriptDir)
  val url = "jdbc:h2:mem:base;INIT=" + initScripts.map(s"runscript from '$scriptDir"+_+"'").mkString("\\;")
  val jdbcDriver =  "org.h2.Driver"
  val slickProfile = slick.jdbc.H2Profile
}
