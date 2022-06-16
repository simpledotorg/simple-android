package org.simple.clinic.home.overdue.search

import org.simple.clinic.home.overdue.search.OverdueSearchQueryValidator.Result

sealed class OverdueSearchEvent

data class OverdueSearchHistoryLoaded(val searchHistory: Set<String>) : OverdueSearchEvent()

data class OverdueSearchQueryChanged(val searchQuery: String) : OverdueSearchEvent()

data class OverdueSearchQueryValidated(val result: Result) : OverdueSearchEvent()
