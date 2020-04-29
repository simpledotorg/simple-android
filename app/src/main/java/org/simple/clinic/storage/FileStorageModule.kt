package org.simple.clinic.storage

import android.app.Application
import dagger.Module
import dagger.Provides
import org.simple.clinic.storage.files.AndroidFileStorage
import org.simple.clinic.storage.files.FileOperations
import org.simple.clinic.storage.files.FileStorage

@Module
class FileStorageModule {

  @Provides
  fun fileStorage(application: Application, fileOperations: FileOperations): FileStorage {
    return AndroidFileStorage(application, fileOperations)
  }
}
