package org.simple.clinic.benchmark

import android.app.Application
import java.io.File
import javax.inject.Inject

class BackupBenchmarkDatabase @Inject constructor(
    private val application: Application,
    private val database: org.simple.clinic.AppDatabase
) {

  fun backup() {
    val databaseDirectory = File(database.openHelper.readableDatabase.path).parentFile!!
    val databaseBackupDirectory = resolveDatabaseBackupDirectory()
    databaseDirectory.copyRecursively(databaseBackupDirectory, overwrite = true)
  }

  fun restore() {
    val databaseDirectory = File(database.openHelper.readableDatabase.path).parentFile!!
    val databaseBackupDirectory = resolveDatabaseBackupDirectory()

    database.close()
    databaseDirectory.deleteRecursively()
    databaseDirectory.mkdirs()
    databaseBackupDirectory.copyRecursively(databaseDirectory, overwrite = true)
  }

  private fun resolveDatabaseBackupDirectory(): File {
    val directory = application.filesDir.resolve("test_db_backup")

    if (!directory.exists()) {
      directory.mkdirs()
    }

    return directory
  }
}
