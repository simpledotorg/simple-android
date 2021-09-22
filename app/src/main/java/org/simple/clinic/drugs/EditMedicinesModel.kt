package org.simple.clinic.drugs

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyLabel
import org.simple.clinic.protocol.ProtocolDrugAndDosages
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import java.util.UUID

@Parcelize
data class EditMedicinesModel(
    val patientUuid: UUID,
    val prescribedDrugs: List<PrescribedDrug>?,
    val protocolDrugs: List<ProtocolDrugAndDosages>?,
    val editMedicineButtonState: EditMedicineButtonState?,
    val medicineFrequencyToLabelMap: Map<MedicineFrequency?, DrugFrequencyLabel>?
) : Parcelable {

  companion object {
    fun create(patientUuid: UUID): EditMedicinesModel {
      return EditMedicinesModel(patientUuid, null, null, null, null)
    }
  }

  val hasPrescribedAndProtocolDrugs
    get() = prescribedDrugs != null && protocolDrugs != null

  val hasMedicineFrequencyToLabelMap
    get() = medicineFrequencyToLabelMap != null

  fun isProtocolDrug(prescribedDrug: PrescribedDrug): Boolean {
    return protocolDrugs!!.any { it.matches(prescribedDrug) }
  }

  fun prescribedDrugsFetched(listFetched: List<PrescribedDrug>) =
      copy(prescribedDrugs = listFetched)

  fun protocolDrugsFetched(listFetched: List<ProtocolDrugAndDosages>) =
      copy(protocolDrugs = listFetched)

  fun editMedicineDrugStateFetched(editMedicineButtonState: EditMedicineButtonState) =
      copy(editMedicineButtonState = editMedicineButtonState)

  fun medicineFrequencyToLabelMapLoaded(medicineFrequencyToLabelMap: Map<MedicineFrequency?, DrugFrequencyLabel>?) =
      copy(medicineFrequencyToLabelMap = medicineFrequencyToLabelMap)
}
