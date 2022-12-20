package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_106 @Inject constructor() : Migration(105, 106) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL("""
        CREATE TABLE IF NOT EXISTS "Questionnaire" (
          "uuid" TEXT NOT NULL, 
          "questionnaire_type" TEXT NOT NULL, 
          "layout" TEXT NOT NULL,
          PRIMARY KEY("questionnaire_type")
        )
      """)
    }
  }
}
