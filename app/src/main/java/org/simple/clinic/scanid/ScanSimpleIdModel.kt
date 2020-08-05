package org.simple.clinic.scanid

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ScanSimpleIdModel(
    val shortCode: ShortCodeInput?
) : Parcelable {

  companion object {
    fun create() = ScanSimpleIdModel(shortCode = null)
  }

  fun shortCodeChanged(shortCode: ShortCodeInput): ScanSimpleIdModel {
    return copy(shortCode = shortCode)
  }
}
