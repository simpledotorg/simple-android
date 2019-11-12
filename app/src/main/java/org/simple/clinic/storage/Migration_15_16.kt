package org.simple.clinic.storage

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration

@Suppress("ClassName")
class Migration_15_16 @javax.inject.Inject constructor() : Migration(15, 16) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""DROP TABLE "PatientFuzzySearch"""")
  }
}
