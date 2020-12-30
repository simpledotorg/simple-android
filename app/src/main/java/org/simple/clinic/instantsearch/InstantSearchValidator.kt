package org.simple.clinic.instantsearch

import org.simple.clinic.instantsearch.InstantSearchValidator.Result.Empty
import org.simple.clinic.instantsearch.InstantSearchValidator.Result.LengthTooShort
import org.simple.clinic.instantsearch.InstantSearchValidator.Result.Valid
import javax.inject.Inject

class InstantSearchValidator @Inject constructor() {

  sealed class Result {
    data class Valid(val searchQuery: String) : Result()

    object LengthTooShort : Result()

    object Empty : Result()
  }

  fun validate(searchQuery: String, minLengthForSearchQuery: Int): Result {
    if (searchQuery.isBlank()) return Empty

    return if (searchQuery.length >= minLengthForSearchQuery) {
      Valid(searchQuery)
    } else {
      LengthTooShort
    }
  }
}
