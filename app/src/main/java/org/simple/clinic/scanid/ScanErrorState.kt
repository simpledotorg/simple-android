package org.simple.clinic.scanid

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class ScanErrorState : Parcelable {

  @Parcelize
  data object InvalidQrCode : ScanErrorState()

  @Parcelize
  data object IdentifierAlreadyExists : ScanErrorState()
}
