package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("Classname")
class Migration_69 @Inject constructor() : Migration(68, 69) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL("""
        ALTER TABLE "Patient" ADD COLUMN "registeredFacilityId" TEXT DEFAULT NULL 
        """
      )

      execSQL("""
        ALTER TABLE "Patient" ADD COLUMN "assignedFacilityId" TEXT DEFAULT NULL
      """)

    }
  }
}
