package org.simple.clinic.teleconsultlog.shareprescription

import android.graphics.Bitmap
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.patient.Patient

sealed class TeleconsultSharePrescriptionEvent

data class PatientDetailsLoaded(val patient: Patient) : TeleconsultSharePrescriptionEvent()

data class PatientMedicinesLoaded(val medicines: List<PrescribedDrug>) : TeleconsultSharePrescriptionEvent()

data class SignatureLoaded(val bitmap: Bitmap) : TeleconsultSharePrescriptionEvent()

data class MedicalRegistrationIdLoaded(val medicalRegistrationId: String) : TeleconsultSharePrescriptionEvent()
