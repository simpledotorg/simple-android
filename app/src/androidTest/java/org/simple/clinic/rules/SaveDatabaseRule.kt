package org.simple.clinic.rules

import android.app.Application
import android.util.Log
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import java.io.File
import javax.inject.Inject

class SaveDatabaseRule : TestWatcher() {

  @Inject
  lateinit var application: Application

  @Inject
  lateinit var database: AppDatabase

  init {
    TestClinicApp.appComponent().inject(this@SaveDatabaseRule)
  }

  override fun failed(e: Throwable, description: Description) {
    super.failed(e, description)
    val outputDirectory = resolveDatabaseStorageDirectory(description.className, description.methodName)
    val databaseDirectory = File(database.openHelper.readableDatabase.path).parentFile!!

    databaseDirectory.copyRecursively(outputDirectory, overwrite = true)
    Log.i("TestRunner", "Test databases copied to ${outputDirectory.path}")
  }

  private fun resolveDatabaseStorageDirectory(
      testClassName: String,
      testMethodName: String
  ): File {
    val directory = application.getExternalFilesDir(null)!!
        .resolve("test_db")
        .resolve(testClassName)
        .resolve(testMethodName)

    if (!directory.exists()) {
      directory.mkdirs()
    }

    return directory
  }

  override fun finished(description: Description?) {
    super.finished(description)
    database.clearAllTables()
  }
}
