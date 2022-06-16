package org.simple.clinic.home.overdue.search

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OverdueSearchModel(
    val overdueSearchHistory: Set<String>?
) : Parcelable {

  companion object {

    fun create(): OverdueSearchModel {
      return OverdueSearchModel(overdueSearchHistory = null)
    }
  }

  fun overdueSearchHistoryLoaded(
      overdueSearchHistory: Set<String>
  ): OverdueSearchModel {
    return copy(overdueSearchHistory = overdueSearchHistory)
  }
}
