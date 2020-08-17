package org.simple.clinic.searchresultsview

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class SearchResultsModel : Parcelable {

  companion object {
    fun create(): SearchResultsModel = SearchResultsModel()
  }
}
