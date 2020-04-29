package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject

@Suppress("ClassName")
class Migration_38_39 @Inject constructor() : Migration(38, 39) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.execSQL("""
      UPDATE Patient
      SET status = (
        CASE
          WHEN status == 'ACTIVE' THEN 'active'
          WHEN status == 'DEAD' THEN 'dead'
          WHEN status == 'MIGRATED' THEN 'migrated'
          WHEN status == 'UNRESPONSIVE' THEN 'unresponsive'
          WHEN status == 'INACTIVE' THEN 'inactive'
        END
      )
    """)
  }
}
