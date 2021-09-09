package org.simple.clinic.teleconsultlog.prescription.medicines

import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyChoiceItem
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import java.util.UUID

interface TeleconsultMedicinesUiActions {
  fun openEditMedicines(patientUuid: UUID)
  fun openDrugDurationSheet(prescription: PrescribedDrug)
  fun openDrugFrequencySheet(
      prescription: PrescribedDrug,
      medicineFrequencyToFrequencyChoiceItemMap: Map<MedicineFrequency?, DrugFrequencyChoiceItem>
  )
}
