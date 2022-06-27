package org.simple.clinic.home.overdue.search

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Observable
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.OverdueSearchHistory
import javax.inject.Inject

private const val SEARCH_HISTORY_SEPARATOR = ", "

class OverdueSearchHistory @Inject constructor(
    @TypedPreference(OverdueSearchHistory)
    private val overdueSearchHistoryPreference: Preference<String>,
    private val searchConfig: OverdueSearchConfig
) {

  fun fetch(): Observable<Set<String>> {
    return overdueSearchHistoryPreference
        .asObservable()
        .map { it.splitToSet() }
  }

  fun add(searchQuery: String) {
    val searchHistory = overdueSearchHistoryPreference.get().splitToSet()
    val searchHistoryLimit = if (searchHistory.size >= searchConfig.searchHistoryLimit) {
      // We are subtracting 1 to ensure our search history is within the specified limit.
      // To avoid having saving all the search queries, we are dropping older search queries that are
      // outside the limit
      searchConfig.searchHistoryLimit - 1
    } else {
      searchHistory.size
    }

    val updatedSearchHistory = setOf(searchQuery) + searchHistory.take(searchHistoryLimit)
    overdueSearchHistoryPreference.set(updatedSearchHistory.joinToString(SEARCH_HISTORY_SEPARATOR))
  }

  private fun String.splitToSet(): Set<String> {
    return split(SEARCH_HISTORY_SEPARATOR).filter { it.isNotBlank() }.toSet()
  }
}
