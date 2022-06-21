package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_104 @Inject constructor() : Migration(103, 104) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL("CREATE INDEX IF NOT EXISTS `index_Appointment_facilityUuid` ON `Appointment` (`facilityUuid`)")
    }
  }
}
