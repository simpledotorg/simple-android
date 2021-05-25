package org.simple.clinic.storage.migrations

import android.app.Application
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import java.io.File
import javax.inject.Inject

@Suppress("ClassName")
class Migration_68 @Inject constructor(
    private val application: Application
) : Migration(67, 68) {

  override fun migrate(database: SupportSQLiteDatabase) {
    val reportsFile = file("report.html")
    val helpFile = file("help.html")

    database.inTransaction {
      execSQL("""
        CREATE TABLE IF NOT EXISTS "TextRecords" (
          "id" TEXT NOT NULL, 
          "text" TEXT, 
          PRIMARY KEY("id")
        )
      """)

      if (isContentPresent(reportsFile)) {
        saveFileContent(this, "reports", reportsFile)
      }

      if (isContentPresent(helpFile)) {
        saveFileContent(this, "help", helpFile)
      }
    }

    reportsFile.delete()
    helpFile.delete()
  }

  private fun file(path: String): File {
    return application.filesDir.resolve(path)
  }

  private fun isContentPresent(file: File): Boolean {
    return file.exists() && file.isFile && file.length() > 0
  }

  private fun saveFileContent(
      db: SupportSQLiteDatabase,
      id: String,
      file: File
  ) {
    // Using a prepared statement here because the saved files can
    // have quotes (') in them which breaks the sqlite insert unless
    // we escape it. The prepared statement here will escape it for us.
    val statement = db
        .compileStatement(
            """
          INSERT INTO "TextRecords" ("id", "text") 
          VALUES (?, ?)
        """
        ).apply {
          bindString(1, id)
          bindString(2, file.readText())
        }

    statement.executeInsert()
  }
}
