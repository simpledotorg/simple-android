package org.simple.clinic.storage

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration

@Suppress("ClassName")
class Migration_15_16 : Migration(15, 16) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""DROP TABLE "PatientFuzzySearch"""")
  }
}
