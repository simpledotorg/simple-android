package org.simple.clinic.scanid.scannedqrcode

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.patient.businessid.Identifier

@Parcelize
data class ScannedQrCodeModel(
    val identifier: Identifier
) : Parcelable {

  companion object {
    fun create(identifier: Identifier): ScannedQrCodeModel {
      return ScannedQrCodeModel(
          identifier = identifier
      )
    }
  }
}
