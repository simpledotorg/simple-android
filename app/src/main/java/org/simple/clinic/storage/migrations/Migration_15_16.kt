package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject

@Suppress("ClassName")
class Migration_15_16 @Inject constructor() : Migration(15, 16) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""DROP TABLE "PatientFuzzySearch"""")
  }
}
