package org.simple.clinic.storage.files

import android.app.Application
import java.io.File
import javax.inject.Inject

class AndroidFileStorage @Inject constructor(
    private val application: Application
) : FileStorage {
  override fun getWriteableFile(name: String): GetFileResult {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun writeToFile(file: File, string: String): WriteFileResult {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun readFromFile(file: File): ReadFileResult {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}
