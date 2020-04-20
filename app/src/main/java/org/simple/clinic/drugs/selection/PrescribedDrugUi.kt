package org.simple.clinic.drugs.selection

import com.xwray.groupie.ViewHolder
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.summary.GroupieItemWithUiEvents
import java.util.UUID

interface PrescribedDrugUi {
  fun populateDrugsList(protocolDrugItems: List<GroupieItemWithUiEvents<out ViewHolder>>)
  fun showDosageSelectionSheet(drugName: String, patientUuid: UUID, prescribedDrugUuid: UUID?)
  fun showNewPrescriptionEntrySheet(patientUuid: UUID)
  fun showUpdateCustomPrescriptionSheet(prescribedDrug: PrescribedDrug)
  fun goBackToPatientSummary()
}
