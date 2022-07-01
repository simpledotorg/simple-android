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
    val overdueSearchProgressState: OverdueSearchProgressState?,
    @IgnoredOnParcel
    val overdueSearchResults: PagingData<OverdueAppointment> = PagingData.empty()
) : Parcelable {

  val hasSearchQuery: Boolean
    get() = !searchQuery.isNullOrBlank()

  companion object {

    fun create(): OverdueSearchModel {
      return OverdueSearchModel(
          overdueSearchHistory = null,
          searchQuery = null,
          overdueSearchProgressState = null
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

  fun loadStateChanged(overdueSearchProgressState: OverdueSearchProgressState): OverdueSearchModel {
    return copy(overdueSearchProgressState = overdueSearchProgressState)
  }

  fun overdueSearchResultsLoaded(overdueSearchResults: PagingData<OverdueAppointment>): OverdueSearchModel {
    return copy(overdueSearchResults = overdueSearchResults)
  }
}
