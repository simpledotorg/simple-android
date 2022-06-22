package org.simple.clinic.home.overdue.search

import android.os.Parcelable
import androidx.paging.PagingData
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.simple.clinic.home.overdue.OverdueAppointment

@Parcelize
data class OverdueSearchModel(
    val overdueSearchHistory: Set<String>?,
    val searchQuery: String?,
    @IgnoredOnParcel
    val overdueSearchResults: PagingData<OverdueAppointment>? = null
) : Parcelable {

  val hasSearchQuery: Boolean
    get() = !searchQuery.isNullOrBlank()

  val hasOverdueSearchResults: Boolean
    get() = overdueSearchResults != null

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

  fun overdueSearchResultsLoaded(overdueSearchResults: PagingData<OverdueAppointment>): OverdueSearchModel {
    return copy(overdueSearchResults = overdueSearchResults)
  }
}
