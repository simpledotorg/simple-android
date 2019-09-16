package org.simple.clinic.mobius.migration

import org.simple.clinic.remoteconfig.ConfigReader

data class MobiusMigrationConfig(
    val useAllPatientsInFacilityView: Boolean
) {
  companion object {
    fun read(reader: ConfigReader): MobiusMigrationConfig {
      val useAllPatientsInFacilityView = reader
          .boolean("mobius_use_all_patients_in_facility_view", default = false)
      return MobiusMigrationConfig(useAllPatientsInFacilityView = useAllPatientsInFacilityView)
    }
  }
}
