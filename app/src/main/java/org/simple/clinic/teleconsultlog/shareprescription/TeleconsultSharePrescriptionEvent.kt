package org.simple.clinic.teleconsultlog.shareprescription

import android.graphics.Bitmap
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.patient.Patient
import org.simple.clinic.teleconsultlog.prescription.TeleconsultPrescriptionEvent
import org.simple.clinic.widgets.UiEvent

sealed class TeleconsultSharePrescriptionEvent

data class PatientDetailsLoaded(val patient: Patient) : TeleconsultSharePrescriptionEvent()

data class PatientMedicinesLoaded(val medicines: List<PrescribedDrug>) : TeleconsultSharePrescriptionEvent()

data class SignatureLoaded(val bitmap: Bitmap) : TeleconsultSharePrescriptionEvent()

data class MedicalRegistrationIdLoaded(val medicalRegistrationId: String) : TeleconsultSharePrescriptionEvent()

object DoneClicked : TeleconsultSharePrescriptionEvent()
