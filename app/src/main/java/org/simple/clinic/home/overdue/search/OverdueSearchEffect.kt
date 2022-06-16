package org.simple.clinic.home.overdue.search

sealed class OverdueSearchEffect

object LoadOverdueSearchHistory : OverdueSearchEffect()

data class ValidateOverdueSearchQuery(val searchQuery: String) : OverdueSearchEffect()
