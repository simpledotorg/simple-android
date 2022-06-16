package org.simple.clinic.home.overdue.search

sealed class OverdueSearchEvent

data class OverdueSearchHistoryLoaded(val searchHistory: Set<String>) : OverdueSearchEvent()
