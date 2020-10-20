package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_82 @Inject constructor() : Migration(81, 82) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL(""" ALTER TABLE "TeleconsultRecord" ADD COLUMN "request_requestCompleted" TEXT DEFAULT NULL """)
    }
  }
}
