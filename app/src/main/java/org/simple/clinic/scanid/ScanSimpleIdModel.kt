package org.simple.clinic.scanid

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.scanid.ScanSearchState.NotSearching
import org.simple.clinic.scanid.ScanSearchState.Searching

@Parcelize
data class ScanSimpleIdModel(
    val shortCode: ShortCodeInput?,
    val scanSearchState: ScanSearchState
) : Parcelable {

  companion object {
    fun create() = ScanSimpleIdModel(shortCode = null, scanSearchState = NotSearching)
  }

  fun shortCodeChanged(shortCode: ShortCodeInput): ScanSimpleIdModel {
    return copy(shortCode = shortCode)
  }

  fun searching(): ScanSimpleIdModel {
    return copy(scanSearchState = Searching)
  }

  fun notSearching(): ScanSimpleIdModel {
    return copy(scanSearchState = NotSearching)
  }
}
