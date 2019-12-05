package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_52_53 @Inject constructor() : Migration(52, 53) {
  override fun migrate(database: SupportSQLiteDatabase) {
    with(database) {
      inTransaction {
        execSQL("""ALTER TABLE "PatientAddress" ADD COLUMN "streetAddress" TEXT DEFAULT NULL""")
        execSQL("""ALTER TABLE "PatientAddress" ADD COLUMN "zone" TEXT DEFAULT NULL""")
      }
    }
  }
}
