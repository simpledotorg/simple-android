package org.simple.clinic.scanid

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class ScanSearchState : Parcelable {

  @Parcelize
  data object Searching : ScanSearchState()

  @Parcelize
  data object NotSearching : ScanSearchState()
}
