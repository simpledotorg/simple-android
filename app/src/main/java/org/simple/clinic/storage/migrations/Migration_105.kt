package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_105 @Inject constructor() : Migration(104, 105) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL("""
       ALTER TABLE "CallResult"
       ADD COLUMN "patientId" TEXT DEFAULT NULL  
     """.trimIndent())

      execSQL("""
       ALTER TABLE "CallResult"
       ADD COLUMN "facilityId" TEXT DEFAULT NULL  
     """.trimIndent())
    }
  }
}
