package org.simple.clinic.patient

import androidx.annotation.VisibleForTesting
import org.simple.clinic.remoteconfig.ConfigReader

data class PatientConfig(
    val limitOfSearchResults: Int,
    val recentPatientLimit: Int
) {

  companion object {

    /**
     * See the [SQLite Limits](https://www.sqlite.org/limits.html) doc for more information.
     *
     * See the section "maximum number of host parameters in a single SQL statement".
     * */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    const val MAXIMUM_SQLITE_QUERY_LIMIT = 1000L

    fun read(configReader: ConfigReader): PatientConfig {
      val numberOfSearchResults = configReader
          .long("patients_searchresults_limit", 100L)
          .coerceAtLeast(1L)
          .coerceAtMost(MAXIMUM_SQLITE_QUERY_LIMIT)
          .toInt()

      val numberOfRecentPatients = configReader
          .long("patients_recentpatients_limit", 10L)
          .coerceAtLeast(1L)
          .toInt()

      return PatientConfig(
          limitOfSearchResults = numberOfSearchResults,
          recentPatientLimit = numberOfRecentPatients
      )
    }
  }
}
