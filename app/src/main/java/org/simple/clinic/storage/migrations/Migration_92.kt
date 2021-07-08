package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_92 @Inject constructor() : Migration(91, 92) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL("""
        CREATE TABLE IF NOT EXISTS "Drug" (
          "id" TEXT NOT NULL, "name" TEXT NOT NULL, "category" TEXT,
          "frequency" TEXT, "composition" TEXT, "dosage" TEXT, "rxNormCode" TEXT,
          "protocol" TEXT NOT NULL, "common" TEXT NOT NULL,
          "createdAt" TEXT NOT NULL, "updatedAt" TEXT NOT NULL, "deletedAt" TEXT, 
          PRIMARY KEY("id")
        )
      """)
    }
  }
}
