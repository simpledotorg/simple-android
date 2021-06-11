package org.simple.clinic.instantsearch

import org.simple.clinic.remoteconfig.ConfigReader

data class InstantSearchConfig(
    val minLengthOfSearchQuery: Int,
    val pagingLoadSize: Int
) {

  companion object {
    fun read(configReader: ConfigReader): InstantSearchConfig {
      val minLengthOfSearchQuery = configReader.long("min_length_of_search_query", 2)
      val instantSearchPageSize = configReader.long("instant_search_page_size", 15)

      return InstantSearchConfig(
          minLengthOfSearchQuery = minLengthOfSearchQuery.toInt(),
          pagingLoadSize = instantSearchPageSize.toInt(),
      )
    }
  }
}
