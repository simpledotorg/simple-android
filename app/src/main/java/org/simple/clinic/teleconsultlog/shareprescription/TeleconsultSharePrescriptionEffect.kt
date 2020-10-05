package org.simple.clinic.teleconsultlog.shareprescription

import android.graphics.Bitmap
import java.time.LocalDate
import java.util.UUID

sealed class TeleconsultSharePrescriptionEffect

data class LoadPatientDetails(val patientUuid: UUID) : TeleconsultSharePrescriptionEffect()

data class LoadPatientMedicines(val patientUuid: UUID) : TeleconsultSharePrescriptionEffect()

object LoadSignature : TeleconsultSharePrescriptionEffect()

data class SetSignature(val bitmap: Bitmap) : TeleconsultSharePrescriptionEffect()

object LoadMedicalRegistrationId : TeleconsultSharePrescriptionEffect()

data class SetMedicalRegistrationId(val medicalRegistrationId: String) : TeleconsultSharePrescriptionEffect()

data class SaveBitmapInExternalStorage(val bitmap: Bitmap) : TeleconsultSharePrescriptionEffect()

object GoToHomeScreen : TeleconsultSharePrescriptionEffect()

data class LoadPatientProfile(val patientUuid: UUID) : TeleconsultSharePrescriptionEffect()

