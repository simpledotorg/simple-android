package org.simple.clinic.teleconsultlog.shareprescription

import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.platform.util.RuntimePermissionResult
import org.simple.clinic.util.Optional
import org.simple.clinic.util.RequiresPermission
import org.simple.clinic.widgets.UiEvent

sealed class TeleconsultSharePrescriptionEvent : UiEvent

data class PatientMedicinesLoaded(val medicines: List<PrescribedDrug>) : TeleconsultSharePrescriptionEvent()

data class SignatureLoaded(val bitmap: Bitmap) : TeleconsultSharePrescriptionEvent()

data class MedicalRegistrationIdLoaded(val medicalRegistrationId: String) : TeleconsultSharePrescriptionEvent()

data class DownloadClicked(
    val bitmap: Bitmap,
    override var permission: Optional<RuntimePermissionResult> = Optional.empty(),
    override val permissionString: String = Manifest.permission.WRITE_EXTERNAL_STORAGE,
    override val permissionRequestCode: Int = 1
) : TeleconsultSharePrescriptionEvent(), RequiresPermission {
  override val analyticsName: String = "Share Prescription Screen:Download Clicked"
}

object PrescriptionImageSaved : TeleconsultSharePrescriptionEvent()

object DoneClicked : TeleconsultSharePrescriptionEvent() {
  override val analyticsName: String = "Share Prescription Screen:Done Clicked"
}

data class PatientProfileLoaded(val patientProfile: PatientProfile) : TeleconsultSharePrescriptionEvent()

data class ShareClicked(
    val bitmap: Bitmap,
    override var permission: Optional<RuntimePermissionResult> = Optional.empty(),
    override val permissionString: String = Manifest.permission.WRITE_EXTERNAL_STORAGE,
    override val permissionRequestCode: Int = 2
) : TeleconsultSharePrescriptionEvent(), RequiresPermission {
  override val analyticsName: String = "Share Prescription Screen:Share Clicked"
}

data class PrescriptionSavedForSharing(val fileName: String) : TeleconsultSharePrescriptionEvent()

data class SharePrescriptionUri(val imageUri: Uri) : TeleconsultSharePrescriptionEvent()

object BackClicked : TeleconsultSharePrescriptionEvent() {
  override val analyticsName: String = "Share Prescription Screen:Back Clicked"
}
