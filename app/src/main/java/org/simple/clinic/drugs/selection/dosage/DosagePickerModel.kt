package org.simple.clinic.drugs.selection.dosage

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.protocol.ProtocolDrug
import java.util.UUID

@Parcelize
data class DosagePickerModel(
    val drugName: String,
    val protocolDrugs: List<ProtocolDrug>?,
    val existingPrescriptionUuid: UUID?
) : Parcelable {

  companion object {

    fun create(
        drugName: String,
        existingPrescriptionUuid: UUID?
    ): DosagePickerModel {
      return DosagePickerModel(
          drugName = drugName,
          protocolDrugs = null,
          existingPrescriptionUuid = existingPrescriptionUuid
      )
    }
  }

  val hasLoadedProtocolDrugs: Boolean
    get() = protocolDrugs != null

  val hasExistingPrescription: Boolean
    get() = existingPrescriptionUuid != null

  fun protocolDrugsLoaded(protocolDrugs: List<ProtocolDrug>): DosagePickerModel {
    return copy(protocolDrugs = protocolDrugs)
  }
}
