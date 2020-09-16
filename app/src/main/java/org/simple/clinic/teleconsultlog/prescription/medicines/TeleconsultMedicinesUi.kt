package org.simple.clinic.teleconsultlog.prescription.medicines

import org.simple.clinic.drugs.PrescribedDrug

interface TeleconsultMedicinesUi {
  fun renderMedicines(medicines: List<PrescribedDrug>)
  fun showNoMedicines()
}
