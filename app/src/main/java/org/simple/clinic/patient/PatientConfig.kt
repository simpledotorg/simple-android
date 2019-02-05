package org.simple.clinic.patient

/**
 * See the [SQLite Limits](https://www.sqlite.org/limits.html) doc for more information.
 *
 * See the section "maximum number of host parameters in a single SQL statement".
 * */
const val MAXIMUM_SQLITE_QUERY_LIMIT = 1000

data class PatientConfig(val limitOfSearchResults: Int, val scanSimpleCardFeatureEnabled: Boolean) {
  init {
    if (limitOfSearchResults !in 1 until MAXIMUM_SQLITE_QUERY_LIMIT) {
      throw AssertionError("limit of search results must be within [1, 999]")
    }
  }
}
