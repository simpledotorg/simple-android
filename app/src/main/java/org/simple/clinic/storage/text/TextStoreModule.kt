package org.simple.clinic.storage.text

import dagger.Module
import dagger.Provides
import org.simple.clinic.storage.files.FileStorage
import javax.inject.Named

@Module
class TextStoreModule {

  @Provides
  fun textStore(
      fileStorage: FileStorage,
      @Named("reports_file_path") reportsFilePath: String,
      @Named("help_file_path") helpFilePath: String
  ): TextStore {
    return LocalFileTextStore(
        fileStorage = fileStorage,
        keysToFilePath = mapOf(
            "reports" to reportsFilePath,
            "help" to helpFilePath
        )
    )
  }
}
