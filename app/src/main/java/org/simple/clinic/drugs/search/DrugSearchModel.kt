package org.simple.clinic.drugs.search

data class DrugSearchModel(
    val searchQuery: String
) {

  companion object {

    fun create() = DrugSearchModel(
        searchQuery = ""
    )
  }

  val hasSearchQuery
    get() = searchQuery.isNotBlank()

  fun searchQueryChanged(searchQuery: String): DrugSearchModel {
    return copy(searchQuery = searchQuery)
  }
}
