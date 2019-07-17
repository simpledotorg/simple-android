package org.simple.clinic.storage

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Suppress("ClassName")
class Migration_40_41 : Migration(40, 41) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""
      UPDATE "Patient"
      SET
      "gender" = (
          CASE
            WHEN "gender" == 'MALE' THEN 'male'
            WHEN "gender" == 'FEMALE' THEN 'female'
            WHEN "gender" == 'TRANSGENDER' THEN 'transgender'
          END
        )
    """)
  }
}
