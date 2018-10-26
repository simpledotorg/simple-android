package org.simple.clinic.storage

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration

@Suppress("ClassName")
class Migration_16_17 : Migration(16, 17) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""DROP INDEX "index_Patient_addressUuid"""")
    database.execSQL("""CREATE INDEX "index_Patient_addressUuid" ON "Patient" ("addressUuid")""")
  }
}
