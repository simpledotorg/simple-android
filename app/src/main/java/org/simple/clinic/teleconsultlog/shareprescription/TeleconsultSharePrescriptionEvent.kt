package org.simple.clinic.teleconsultlog.shareprescription

import android.graphics.Bitmap
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.widgets.UiEvent
import java.time.LocalDate

sealed class TeleconsultSharePrescriptionEvent : UiEvent

data class PatientDetailsLoaded(val patient: Patient) : TeleconsultSharePrescriptionEvent()

data class PatientMedicinesLoaded(val medicines: List<PrescribedDrug>) : TeleconsultSharePrescriptionEvent()

data class SignatureLoaded(val bitmap: Bitmap) : TeleconsultSharePrescriptionEvent()

data class MedicalRegistrationIdLoaded(val medicalRegistrationId: String) : TeleconsultSharePrescriptionEvent()

data class DownloadClicked(val bitmap: Bitmap) : TeleconsultSharePrescriptionEvent() {
  override val analyticsName: String = "Share Prescription Screen:Download Clicked"
}

object PrescriptionImageSaved : TeleconsultSharePrescriptionEvent()

object DoneClicked : TeleconsultSharePrescriptionEvent()

data class PatientProfileLoaded(val patientProfile: PatientProfile) : TeleconsultSharePrescriptionEvent()

