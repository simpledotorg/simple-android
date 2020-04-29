package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject

@Suppress("ClassName")
class Migration_41_42 @Inject constructor() : Migration(41, 42) {

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
