package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_95 @Inject constructor() : Migration(94, 95) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL("""
        CREATE TABLE IF NOT EXISTS "CallResult" (
          "id" TEXT NOT NULL, 
          "userId" TEXT NOT NULL, 
          "appointmentId" TEXT NOT NULL, 
          "removeReason" TEXT, 
          "outcome" TEXT NOT NULL, 
          "createdAt" TEXT NOT NULL, 
          "updatedAt" TEXT NOT NULL, 
          "deletedAt" TEXT, 
          PRIMARY KEY("id")
        )
        """)
    }
  }
}
