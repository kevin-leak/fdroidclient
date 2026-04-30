package org.fdroid.fdroid.nearby

import java.io.File
import java.io.InputStream

object CopyUtils {

  @JvmStatic
  fun copyInputStreamToFile(inputStream: InputStream, file: File) {
    inputStream.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
  }
}
