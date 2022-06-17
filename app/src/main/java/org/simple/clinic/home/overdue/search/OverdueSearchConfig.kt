package org.simple.clinic.home.overdue.search

import org.simple.clinic.remoteconfig.ConfigReader

data class OverdueSearchConfig(
    val minLengthOfSearchQuery: Int,
    val searchHistoryLimit: Int
) {

  companion object {
    fun read(configReader: ConfigReader): OverdueSearchConfig {
      val minLengthOfSearchQuery = configReader.long("min_length_of_overdue_search_query", 3)
      val searchHistoryLimit = configReader.long("overdue_search_history_limit", 5)

      return OverdueSearchConfig(
          minLengthOfSearchQuery = minLengthOfSearchQuery.toInt(),
          searchHistoryLimit = searchHistoryLimit.toInt()
      )
    }
  }
}
