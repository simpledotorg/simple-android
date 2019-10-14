package org.simple.clinic.mobius.migration

import org.simple.clinic.remoteconfig.ConfigReader

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
