package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject

@Suppress("ClassName")
class Migration_37_38 @Inject constructor() : Migration(37, 38) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""
      UPDATE "Appointment"
      SET "status" = (
        CASE
          WHEN "status" == 'SCHEDULED' THEN 'scheduled'
          WHEN "status" == 'CANCELLED' THEN 'cancelled'
          WHEN "status" == 'VISITED' THEN 'visited'
        END
      )
    """)
  }
}
