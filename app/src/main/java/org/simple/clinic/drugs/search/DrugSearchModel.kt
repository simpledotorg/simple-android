package org.simple.clinic.drugs.search

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DrugSearchModel(
    val searchQuery: String
) : Parcelable {

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
