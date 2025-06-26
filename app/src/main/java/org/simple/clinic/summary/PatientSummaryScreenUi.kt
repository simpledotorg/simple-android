package org.simple.clinic.summary

import org.simple.clinic.cvdrisk.StatinInfo

interface PatientSummaryScreenUi {
  fun renderPatientSummaryToolbar(patientSummaryProfile: PatientSummaryProfile)
  fun populatePatientProfile(patientSummaryProfile: PatientSummaryProfile)
  fun showDiabetesView()
  fun hideDiabetesView()
  fun showTeleconsultButton()
  fun hideTeleconsultButton()
  fun showAssignedFacilityView()
  fun hideAssignedFacilityView()
  fun hideDoneButton()
  fun showTeleconsultLogButton()
  fun hidePatientDiedStatus()
  fun showPatientDiedStatus()
  fun showNextAppointmentCard()
  fun hideNextAppointmentCard()
  fun showClinicalDecisionSupportAlert()
  fun hideClinicalDecisionSupportAlert()
  fun hideClinicalDecisionSupportAlertWithoutAnimation()
  fun updateStatinAlert(statinInfo: StatinInfo)
}
