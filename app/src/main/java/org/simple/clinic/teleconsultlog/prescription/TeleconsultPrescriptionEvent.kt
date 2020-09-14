package org.simple.clinic.teleconsultlog.prescription

import org.simple.clinic.patient.Patient

sealed class TeleconsultPrescriptionEvent

data class PatientDetailsLoaded(val patient: Patient) : TeleconsultPrescriptionEvent()

object BackClicked : TeleconsultPrescriptionEvent()
