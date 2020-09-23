package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("Classname")
class Migration_76 @Inject constructor() : Migration(75, 76) {
  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL("""
        DROP TABLE IF EXISTS "TeleconsultRecordPrescribedDrug"
      """)
    }
  }
}
