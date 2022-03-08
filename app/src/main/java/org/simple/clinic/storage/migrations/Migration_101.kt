package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_101 @Inject constructor() : Migration(100, 101) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL("CREATE INDEX IF NOT EXISTS `index_BloodPressureMeasurement_facilityUuid` ON `BloodPressureMeasurement` (`facilityUuid`)")
      execSQL("CREATE INDEX IF NOT EXISTS `index_BloodSugarMeasurements_facilityUuid` ON `BloodSugarMeasurements` (`facilityUuid`)")
      execSQL("CREATE INDEX IF NOT EXISTS `index_PrescribedDrug_facilityUuid` ON `PrescribedDrug`(`facilityUuid`)")
      execSQL("CREATE INDEX IF NOT EXISTS `index_Appointment_creationFacilityUuid` ON `Appointment`(`creationFacilityUuid`)")
    }
  }
}
