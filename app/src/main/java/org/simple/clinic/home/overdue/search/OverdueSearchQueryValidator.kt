package org.simple.clinic.home.overdue.search

import org.simple.clinic.home.overdue.search.OverdueSearchQueryValidator.Result.Empty
import org.simple.clinic.home.overdue.search.OverdueSearchQueryValidator.Result.LengthTooShort
import org.simple.clinic.home.overdue.search.OverdueSearchQueryValidator.Result.Valid
import javax.inject.Inject

class OverdueSearchQueryValidator @Inject constructor(
    private val overdueSearchConfig: OverdueSearchConfig
) {

  sealed class Result {
    data class Valid(val searchQuery: String) : Result()

    object LengthTooShort : Result()

    object Empty : Result()
  }

  fun validate(searchQuery: String): Result {
    if (searchQuery.isBlank()) return Empty

    return if (searchQuery.length >= overdueSearchConfig.minLengthOfSearchQuery) {
      Valid(searchQuery)
    } else {
      LengthTooShort
    }
  }
}
