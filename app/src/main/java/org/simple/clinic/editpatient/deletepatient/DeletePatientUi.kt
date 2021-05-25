package org.simple.clinic.editpatient.deletepatient

interface DeletePatientUi {
  fun showDeleteReasons(
      patientDeleteReasons: List<PatientDeleteReason>,
      selectedReason: PatientDeleteReason?
  )
}
