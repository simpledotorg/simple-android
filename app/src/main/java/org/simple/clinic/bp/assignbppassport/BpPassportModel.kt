package org.simple.clinic.bp.assignbppassport

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.patient.businessid.Identifier

@Parcelize
data class BpPassportModel(
    val identifier: Identifier
): Parcelable {

  companion object {
    fun create(identifier: Identifier): BpPassportModel {
      return BpPassportModel(
          identifier = identifier
      )
    }
  }
}
