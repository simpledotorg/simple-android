package org.simple.clinic.scanid

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ScanSimpleIdModel(
    val shortCode: ShortCodeInput?,
    val scanSearchState: ScanSearchState
) : Parcelable {

  companion object {
    fun create() = ScanSimpleIdModel(shortCode = null, scanSearchState = ScanSearchState.NotSearching)
  }

  fun shortCodeChanged(shortCode: ShortCodeInput): ScanSimpleIdModel {
    return copy(shortCode = shortCode)
  }
}
