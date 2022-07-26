package org.simple.clinic.overdue

data class OverdueSearchSuggestion(
    val villageNameOrPatientName: String?,
    val isResultPatientName: Boolean
) {
  companion object {
    fun from(villageNameOrPatientName: String, isResultPatientName: Boolean): OverdueSearchSuggestion {
      return OverdueSearchSuggestion(
          villageNameOrPatientName,
          isResultPatientName
      )
    }
  }
}
