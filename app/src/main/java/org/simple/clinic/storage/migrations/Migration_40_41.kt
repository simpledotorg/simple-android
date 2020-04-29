package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject

@Suppress("ClassName")
class Migration_40_41 @Inject constructor() : Migration(40, 41) {

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
