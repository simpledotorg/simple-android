package org.simple.clinic.patient

import org.simple.clinic.remoteconfig.ConfigReader

data class PatientConfig(
    val recentPatientLimit: Int
) {

  companion object {

    fun read(configReader: ConfigReader): PatientConfig {
      val numberOfRecentPatients = configReader
          .long("patients_recentpatients_limit", 10L)
          .coerceAtLeast(1L)
          .toInt()

      return PatientConfig(
          recentPatientLimit = numberOfRecentPatients
      )
    }
  }
}
