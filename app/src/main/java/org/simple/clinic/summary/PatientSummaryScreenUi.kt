package org.simple.clinic.summary

import org.simple.clinic.cvdrisk.StatinInfo
import org.simple.clinic.patientattribute.BMIReading
import org.simple.clinic.summary.ui.CVDRiskInfo

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
  fun updateStatinNudge(statinInfo: StatinInfo)
  fun showBMIView(bmiReading: BMIReading?)
  fun hideBMIView()

  fun updateCVDRiskView(cvdRiskInfo: CVDRiskInfo)
}
