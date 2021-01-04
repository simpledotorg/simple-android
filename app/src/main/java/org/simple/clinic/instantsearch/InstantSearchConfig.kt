package org.simple.clinic.instantsearch

import org.simple.clinic.remoteconfig.ConfigReader

data class InstantSearchConfig(
    val minLengthOfSearchQuery: Int
) {

  companion object {
    fun read(configReader: ConfigReader): InstantSearchConfig {
      val minLengthOfSearchQuery = configReader.long("min_length_of_search_query", 2)

      return InstantSearchConfig(
          minLengthOfSearchQuery = minLengthOfSearchQuery.toInt(),
      )
    }
  }
}
