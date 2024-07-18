package org.simple.clinic.teleconsultlog.shareprescription

import android.graphics.Bitmap
import android.net.Uri
import java.util.UUID

sealed class TeleconsultSharePrescriptionEffect

data class LoadPatientMedicines(val patientUuid: UUID) : TeleconsultSharePrescriptionEffect()

data object LoadSignature : TeleconsultSharePrescriptionEffect()

data class SetSignature(val bitmap: Bitmap) : TeleconsultSharePrescriptionEffect()

data object LoadMedicalRegistrationId : TeleconsultSharePrescriptionEffect()

data class SetMedicalRegistrationId(val medicalRegistrationId: String) : TeleconsultSharePrescriptionEffect()

data class SaveBitmapInExternalStorage(val bitmap: Bitmap) : TeleconsultSharePrescriptionEffect()

data object GoToHomeScreen : TeleconsultSharePrescriptionEffect()

data class LoadPatientProfile(val patientUuid: UUID) : TeleconsultSharePrescriptionEffect()

data class SharePrescriptionAsImage(val bitmap: Bitmap) : TeleconsultSharePrescriptionEffect()

data class RetrievePrescriptionImageUri(val fileName: String) : TeleconsultSharePrescriptionEffect()

data class OpenSharingDialog(val imageUri: Uri) : TeleconsultSharePrescriptionEffect()

data object GoBack : TeleconsultSharePrescriptionEffect()

data object ShowImageSavedToast : TeleconsultSharePrescriptionEffect()
