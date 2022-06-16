package org.simple.clinic.home.overdue.search

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OverdueSearchModel(
    val overdueSearchHistory: Set<String>?,
    val searchQuery: String?
) : Parcelable {

  val hasSearchQuery: Boolean
    get() = !searchQuery.isNullOrBlank()

  companion object {

    fun create(): OverdueSearchModel {
      return OverdueSearchModel(
          overdueSearchHistory = null,
          searchQuery = null
      )
    }
  }

  fun overdueSearchHistoryLoaded(
      overdueSearchHistory: Set<String>
  ): OverdueSearchModel {
    return copy(overdueSearchHistory = overdueSearchHistory)
  }

  fun overdueSearchQueryChanged(searchQuery: String): OverdueSearchModel {
    return copy(searchQuery = searchQuery)
  }
}
