package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject

@Suppress("ClassName")
class Migration_36_37 @Inject constructor() : Migration(36, 37) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""
      UPDATE "LoggedInUser"
      SET "status" = (
        CASE
          WHEN "status" == 'WAITING_FOR_APPROVAL' THEN 'requested'
          WHEN "status" == 'APPROVED_FOR_SYNCING' THEN 'allowed'
          WHEN "status" == 'DISAPPROVED_FOR_SYNCING' THEN 'denied'
        END
      )
    """)
  }
}
