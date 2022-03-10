package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_102 @Inject constructor() : Migration(101, 102) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL("DROP VIEW IF EXISTS `OverdueAppointment`")
      execSQL("CREATE INDEX IF NOT EXISTS `index_Patient_assignedFacilityId` ON `Patient`(`assignedFacilityId`)")
    }
  }
}
