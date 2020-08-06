package org.simple.clinic.scanid

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ScanSimpleIdModel : Parcelable {

  companion object {
    fun create() = ScanSimpleIdModel()
  }
}
