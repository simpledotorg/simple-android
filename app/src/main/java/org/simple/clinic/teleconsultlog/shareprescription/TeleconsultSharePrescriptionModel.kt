package org.simple.clinic.teleconsultlog.shareprescription

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.teleconsultlog.shareprescription.DownloadButtonState.DOWNLOADING
import org.simple.clinic.teleconsultlog.shareprescription.DownloadButtonState.NOT_DOWNLOADING
import org.simple.clinic.teleconsultlog.shareprescription.ShareButtonState.NOT_SHARING
import org.simple.clinic.teleconsultlog.shareprescription.ShareButtonState.SHARING
import java.time.LocalDate
import java.util.UUID

@Parcelize
data class TeleconsultSharePrescriptionModel(
    val patientUuid: UUID,
    val prescriptionDate: LocalDate,
    val medicines: List<PrescribedDrug>?,
    val medicalRegistrationId: String?,
    val patientProfile: PatientProfile?,
    val downloadButtonState: DownloadButtonState?,
    val shareButtonState: ShareButtonState?
) : Parcelable {

  companion object {
    fun create(
        patientUuid: UUID,
        prescriptionDate: LocalDate,
    ) = TeleconsultSharePrescriptionModel(
        patientUuid = patientUuid,
        prescriptionDate = prescriptionDate,
        medicines = null,
        medicalRegistrationId = null,
        patientProfile = null,
        downloadButtonState = null,
        shareButtonState = null
    )
  }

  val hasPatientProfile: Boolean
    get() = patientProfile != null

  val hasMedicines: Boolean
    get() = medicines != null

  val hasMedicalRegistrationId: Boolean
    get() = medicalRegistrationId != null

  val isPrescriptionDownloading: Boolean
    get() = downloadButtonState == DOWNLOADING

  val isPrescriptionSharing: Boolean
    get() = shareButtonState == SHARING

  fun patientProfileLoaded(patientProfile: PatientProfile): TeleconsultSharePrescriptionModel {
    return copy(patientProfile = patientProfile)
  }

  fun patientMedicinesLoaded(medicines: List<PrescribedDrug>): TeleconsultSharePrescriptionModel {
    return copy(medicines = medicines)
  }

  fun medicalRegistrationIdLoaded(medicalRegistrationId: String): TeleconsultSharePrescriptionModel {
    return copy(medicalRegistrationId = medicalRegistrationId)
  }

  fun downloading(): TeleconsultSharePrescriptionModel {
    return copy(downloadButtonState = DOWNLOADING)
  }

  fun downloadCompleted(): TeleconsultSharePrescriptionModel {
    return copy(downloadButtonState = NOT_DOWNLOADING)
  }

  fun sharing(): TeleconsultSharePrescriptionModel {
    return copy(shareButtonState = SHARING)
  }

  fun sharingCompleted(): TeleconsultSharePrescriptionModel {
    return copy(shareButtonState = NOT_SHARING)
  }
}
