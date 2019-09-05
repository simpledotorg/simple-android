package org.simple.clinic.storage

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Suppress("ClassName")
class Migration_45_46 : Migration(45, 46) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""
      CREATE TABLE IF NOT EXISTS "HomescreenIllustration" (
        "eventId" TEXT NOT NULL,
        "illustrationUrl" TEXT NOT NULL,
        "from_day" INTEGER NOT NULL,
        "from_month" TEXT NOT NULL,
        "to_day" INTEGER NOT NULL,
        "to_month" TEXT NOT NULL,
        PRIMARY KEY("eventId")
      )
    """)
  }
}
