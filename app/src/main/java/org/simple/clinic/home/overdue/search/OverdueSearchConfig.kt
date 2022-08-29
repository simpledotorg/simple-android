package org.simple.clinic.home.overdue.search

import org.simple.clinic.remoteconfig.ConfigReader

data class OverdueSearchConfig(
    val searchHistoryLimit: Int,
    val pagingLoadSize: Int
) {

  companion object {
    fun read(configReader: ConfigReader): OverdueSearchConfig {
      val searchHistoryLimit = configReader.long("overdue_search_history_limit", 5)
      val overdueSearchPageSize = configReader.long("overdue_search_page_size", 15)

      return OverdueSearchConfig(
          searchHistoryLimit = searchHistoryLimit.toInt(),
          pagingLoadSize = overdueSearchPageSize.toInt()
      )
    }
  }
}
