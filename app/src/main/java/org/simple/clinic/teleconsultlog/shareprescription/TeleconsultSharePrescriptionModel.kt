package org.simple.clinic.teleconsultlog.shareprescription

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientProfile
import java.time.LocalDate
import java.util.UUID

@Parcelize
data class TeleconsultSharePrescriptionModel(
    val patientUuid: UUID,
    val patient: Patient?,
    val prescriptionDate: LocalDate,
    val medicines: List<PrescribedDrug>?,
    val medicalRegistrationId: String?,
    val patientProfile: PatientProfile?
) : Parcelable {

  companion object {
    fun create(patientUuid: UUID, prescriptionDate: LocalDate) = TeleconsultSharePrescriptionModel(
        patientUuid = patientUuid,
        patient = null,
        prescriptionDate = prescriptionDate,
        medicines = null,
        medicalRegistrationId = null,
        patientProfile = null
    )
  }

  val hasPatient: Boolean
    get() = patient != null

  val hasPatientProfile: Boolean
    get() = patientProfile != null

  val hasMedicines: Boolean
    get() = medicines != null

  fun patientProfileLoaded(patientProfile: PatientProfile): TeleconsultSharePrescriptionModel {
    return copy(patientProfile = patientProfile)
  }

  fun patientLoaded(patient: Patient?): TeleconsultSharePrescriptionModel {
    return copy(patient = patient)
  }

  fun patientMedicinesLoaded(medicines: List<PrescribedDrug>): TeleconsultSharePrescriptionModel {
    return copy(medicines = medicines)
  }

  fun medicalRegistrationIdLoaded(medicalRegistrationId: String): TeleconsultSharePrescriptionModel {
    return copy(medicalRegistrationId = medicalRegistrationId)
  }
}
