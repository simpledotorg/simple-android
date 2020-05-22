package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_67 @Inject constructor() : Migration(66, 67) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL("""
        ALTER TABLE Patient ADD COLUMN "deletedReason" TEXT DEFAULT NULL
      """)
    }
  }
}
