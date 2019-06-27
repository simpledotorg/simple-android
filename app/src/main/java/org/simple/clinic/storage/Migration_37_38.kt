package org.simple.clinic.storage

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Suppress("ClassName")
class Migration_37_38 : Migration(37, 38) {

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
