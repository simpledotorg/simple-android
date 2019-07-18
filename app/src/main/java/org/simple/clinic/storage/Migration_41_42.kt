package org.simple.clinic.storage

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Suppress("ClassName")
class Migration_41_42 : Migration(41, 42) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""
      UPDATE "PatientPhoneNumber"
      SET
      "phoneType" = (
          CASE
            WHEN "phoneType" == 'MOBILE' THEN 'mobile'
            WHEN "phoneType" == 'LANDLINE' THEN 'landline'
          END
        )
    """)
  }
}
