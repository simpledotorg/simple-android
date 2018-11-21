package org.simple.clinic.patient

/**
 * See the [SQLite Limits](https://www.sqlite.org/limits.html) doc for more information.
 *
 * See the section "maximum number of host parameters in a single SQL statement".
 * */
val maximumSqliteQueryLimit = 1000

data class PatientConfig(val isFuzzySearchV2Enabled: Boolean, val limitOfSearchResults: Int) {
  init {
    if (limitOfSearchResults !in 1 until maximumSqliteQueryLimit) {
      throw AssertionError("limit of searcg results must be within [1, 999]")
    }
  }
}
