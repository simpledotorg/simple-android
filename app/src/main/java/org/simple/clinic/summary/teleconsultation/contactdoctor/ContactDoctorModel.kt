package org.simple.clinic.summary.teleconsultation.contactdoctor

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.summary.teleconsultation.sync.MedicalOfficer
import java.util.UUID

@Parcelize
data class ContactDoctorModel(
    val patientUuid: UUID,
    val medicalOfficers: List<MedicalOfficer>?
) : Parcelable {

  companion object {
    fun create(patientUuid: UUID) = ContactDoctorModel(
        patientUuid = patientUuid,
        medicalOfficers = null
    )
  }

  val hasMedicalOfficers: Boolean
    get() = medicalOfficers != null

  fun medicalOfficersLoaded(medicalOfficers: List<MedicalOfficer>): ContactDoctorModel {
    return copy(medicalOfficers = medicalOfficers)
  }
}
