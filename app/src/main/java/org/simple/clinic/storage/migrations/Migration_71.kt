package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("Classname")
class Migration_71 @Inject constructor() : Migration(70, 71) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL("""
        ALTER TABLE "LoggedInUser" ADD COLUMN "teleconsultPhoneNumber" TEXT DEFAULT NULL 
        """
      )

      execSQL("""
        ALTER TABLE "OngoingLoginEntry" ADD COLUMN "teleconsultPhoneNumber" TEXT DEFAULT NULL 
        """
      )
    }
  }
}
