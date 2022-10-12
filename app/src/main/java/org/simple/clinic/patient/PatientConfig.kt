package org.simple.clinic.patient

import org.simple.clinic.remoteconfig.ConfigReader
import java.time.Period

data class PatientConfig(
    val recentPatientLimit: Int,
    val periodForCalculatingLineListHtnControl: Period
) {

  companion object {

    fun read(configReader: ConfigReader): PatientConfig {
      val numberOfRecentPatients = configReader
          .long("patients_recentpatients_limit", 10L)
          .coerceAtLeast(1L)
          .toInt()

      return PatientConfig(
          recentPatientLimit = numberOfRecentPatients,
          periodForCalculatingLineListHtnControl = Period.ofMonths(2)
      )
    }
  }
}
