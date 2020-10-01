package org.simple.clinic.teleconsultlog.shareprescription

import org.simple.clinic.patient.Patient

sealed class TeleconsultSharePrescriptionEvent

data class PatientDetailsLoaded(val patient: Patient) : TeleconsultSharePrescriptionEvent()
