package org.simple.clinic.drugs

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.protocol.ProtocolDrugAndDosages
import java.util.UUID

@Parcelize
data class EditMedicinesModel(
    val patientUuid: UUID,
    val prescribedDrugs: List<PrescribedDrug>?,
    val protocolDrugs: List<ProtocolDrugAndDosages>?
) : Parcelable {

  companion object {
    fun create(patientUuid: UUID): EditMedicinesModel {
      return EditMedicinesModel(patientUuid, null, null)
    }
  }

  fun prescribedDrugsFetched(listFetched: List<PrescribedDrug>) =
      copy(prescribedDrugs = listFetched)

  fun protocolDrugsFetched(listFetched: List<ProtocolDrugAndDosages>) =
      copy(protocolDrugs = listFetched)
}
