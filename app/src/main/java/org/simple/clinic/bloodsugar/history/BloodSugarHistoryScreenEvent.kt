package org.simple.clinic.bloodsugar.history

import org.simple.clinic.patient.Patient

sealed class BloodSugarHistoryScreenEvent

data class PatientLoaded(val patient: Patient) : BloodSugarHistoryScreenEvent()
