package org.simple.clinic.storage

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration

@Suppress("ClassName")
class Migration_16_17 @javax.inject.Inject constructor() : Migration(16, 17) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""DROP INDEX "index_Patient_addressUuid"""")
    database.execSQL("""CREATE INDEX "index_Patient_addressUuid" ON "Patient" ("addressUuid")""")
  }
}
