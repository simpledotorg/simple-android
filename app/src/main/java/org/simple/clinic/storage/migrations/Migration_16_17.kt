package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject

@Suppress("ClassName")
class Migration_16_17 @Inject constructor() : Migration(16, 17) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""DROP INDEX "index_Patient_addressUuid"""")
    database.execSQL("""CREATE INDEX "index_Patient_addressUuid" ON "Patient" ("addressUuid")""")
  }
}
