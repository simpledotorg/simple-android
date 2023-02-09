package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_108 @Inject constructor() : Migration(107, 108) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL("""
        CREATE TABLE IF NOT EXISTS "QuestionnaireResponse" (
          "uuid" TEXT NOT NULL, 
          "questionnaireId" TEXT NOT NULL, 
          "questionnaireType" TEXT NOT NULL, 
          "facilityId" TEXT NOT NULL, 
          "lastUpdatedByUserId" TEXT NOT NULL, 
          "content" TEXT NOT NULL, 
          "createdAt" TEXT NOT NULL,
          "updatedAt" TEXT NOT NULL,
          "deletedAt" TEXT,
          PRIMARY KEY("uuid")
        )
      """)
    }
  }
}
