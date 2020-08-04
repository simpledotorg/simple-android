package org.simple.clinic.summary.prescribeddrugs

import org.simple.clinic.drugs.PrescribedDrug

interface DrugSummaryUi {
  fun populatePrescribedDrugs(prescribedDrugs: List<PrescribedDrug>)
}
