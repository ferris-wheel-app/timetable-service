package com.ferris.codegen

import java.io.File

object FileUtils {
  def listFiles(path: String): List[String] = {
    val directory = new File(path)

    if (directory.exists && directory.isDirectory) {
      directory.listFiles.filter(_.isFile).map(_.getName).toList
    }
    else {
      Nil
    }
  }
}
