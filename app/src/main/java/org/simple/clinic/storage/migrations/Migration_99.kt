package org.simple.clinic.storage.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.simple.clinic.storage.inTransaction
import javax.inject.Inject

@Suppress("ClassName")
class Migration_99 @Inject constructor() : Migration(98, 99) {

  override fun migrate(database: SupportSQLiteDatabase) {
    database.inTransaction {
      execSQL("CREATE INDEX IF NOT EXISTS `index_Appointment_patientUuid` ON `Appointment` (`patientUuid`)")
      execSQL("CREATE INDEX IF NOT EXISTS `index_BloodSugarMeasurements_patientUuid` ON `BloodSugarMeasurements` (`patientUuid`)")
      execSQL("CREATE INDEX IF NOT EXISTS `index_MedicalHistory_patientUuid` ON `MedicalHistory` (`patientUuid`)")
    }
  }
}
