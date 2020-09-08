package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import org.simple.clinic.teleconsultlog.teleconsultrecord.Answer
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultationType

interface TeleconsultRecordUi {
  fun setTeleconsultationType(teleconsultationType: TeleconsultationType)
  fun setPatientTookMedicines(patientTookMedicines: Answer)
  fun setPatientConsented(patientConsented: Answer)
}
