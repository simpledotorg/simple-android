package org.simple.clinic.storage

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration
import org.simple.clinic.patient.PatientFuzzySearch

@Suppress("ClassName")
class Migration_5_6 : Migration(5, 6) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      // We need to manually create this table because it's a virtual table and Room doesn't support virtual tables (yet!)
      PatientFuzzySearch.createTable(this)
      execSQL("""INSERT INTO "PatientFuzzySearch"("rowid","word") SELECT "rowid","searchableName" FROM "Patient"""")
    }
  }
}
