package org.simple.clinic.drugs

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.protocol.ProtocolDrugAndDosages
import java.util.UUID

@Parcelize
data class EditMedicinesModel(
    val patientUuid: UUID,
    val prescribedDrugs: List<PrescribedDrug>?,
    val protocolDrugs: List<ProtocolDrugAndDosages>?,
    val openIntention: OpenIntention?,
    val editMedicineButtonState: EditMedicineButtonState?
) : Parcelable {

  companion object {
    fun create(patientUuid: UUID, openIntention: OpenIntention): EditMedicinesModel {
      return EditMedicinesModel(patientUuid, null, null, openIntention, null)
    }
  }

  fun isProtocolDrug(prescribedDrug: PrescribedDrug): Boolean {
    return protocolDrugs!!.any { it.matches(prescribedDrug) }
  }

  fun prescribedDrugsFetched(listFetched: List<PrescribedDrug>) =
      copy(prescribedDrugs = listFetched)

  fun protocolDrugsFetched(listFetched: List<ProtocolDrugAndDosages>) =
      copy(protocolDrugs = listFetched)

  fun editMedicineDrugStateFetched(editMedicineButtonState: EditMedicineButtonState) =
      copy(editMedicineButtonState = editMedicineButtonState)
}
