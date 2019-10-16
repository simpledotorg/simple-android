package org.simple.clinic.mobius.migration

import org.simple.clinic.remoteconfig.ConfigReader

@Deprecated("""
  We are no longer using this approach to migrate towards Mobius.
  This package will be deleted as soon as Edit Patient feature becomes stable.""")
data class MobiusMigrationConfig(
    val useEditPatientScreen: Boolean
) {
  companion object {
    fun read(reader: ConfigReader): MobiusMigrationConfig {
      val useEditPatientScreen = reader.boolean("mobius_use_edit_patient_screen", default = false)
      return MobiusMigrationConfig(useEditPatientScreen)
    }
  }
}
