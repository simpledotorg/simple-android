package org.simple.clinic.drugs.selection.dosage

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.protocol.ProtocolDrug

@Parcelize
data class DosagePickerModel(
    val drugName: String,
    val protocolDrugs: List<ProtocolDrug>?
) : Parcelable {

  companion object {

    fun create(
        drugName: String
    ): DosagePickerModel {
      return DosagePickerModel(
          drugName = drugName,
          protocolDrugs = null
      )
    }
  }

  val hasLoadedProtocolDrugs: Boolean
    get() = protocolDrugs != null

  fun protocolDrugsLoaded(protocolDrugs: List<ProtocolDrug>): DosagePickerModel {
    return copy(protocolDrugs = protocolDrugs)
  }
}
