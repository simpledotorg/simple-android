package org.simple.clinic.storage

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Suppress("ClassName")
class Migration_50_51 : Migration(50, 51) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""ALTER TABLE "Encounter" ADD COLUMN "syncStatus" TEXT NOT NULL DEFAULT "DONE" """)

  }
}
