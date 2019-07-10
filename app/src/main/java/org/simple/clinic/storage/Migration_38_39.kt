package org.simple.clinic.storage

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Suppress("ClassName")
class Migration_38_39 : Migration(38, 39) {

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
