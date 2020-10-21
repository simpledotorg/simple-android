package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_83 @Inject constructor() : Migration(82, 83) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL(""" ALTER TABLE TeleconsultRecord RENAME COLUMN "request_requestCompleted" TO "request_requesterCompletionStatus" """)
    }
  }
}
