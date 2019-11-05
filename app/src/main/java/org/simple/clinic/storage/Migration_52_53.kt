package org.simple.clinic.storage

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration_52_53 : Migration(52, 53) {
  override fun migrate(database: SupportSQLiteDatabase) {
    with(database) {
      inTransaction {
        execSQL("""ALTER TABLE "PatientAddress" ADD COLUMN "streetAddress" TEXT DEFAULT NULL""")
        execSQL("""ALTER TABLE "PatientAddress" ADD COLUMN "zone" TEXT DEFAULT NULL""")
      }
    }
  }
}
