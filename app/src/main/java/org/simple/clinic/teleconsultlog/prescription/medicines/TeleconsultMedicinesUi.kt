package org.simple.clinic.teleconsultlog.prescription.medicines

import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyLabel
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency

interface TeleconsultMedicinesUi {
  fun renderMedicines(medicines: List<PrescribedDrug>, medicineFrequencyToLabelMap: Map<MedicineFrequency?, DrugFrequencyLabel>)
  fun showNoMedicines()
  fun showAddButton()
  fun showEditButton()
  fun hideMedicinesRequiredError()
}
