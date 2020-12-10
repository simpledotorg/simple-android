package org.simple.clinic.scanid

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class ScanSearchState : Parcelable {

  @Parcelize
  object Searching : ScanSearchState()

  @Parcelize
  object NotSearching : ScanSearchState()
}
