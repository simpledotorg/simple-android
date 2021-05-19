package org.simple.clinic.scanid

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class ScanErrorState : Parcelable {

  @Parcelize
  object InvalidQrCode : ScanErrorState()
}
