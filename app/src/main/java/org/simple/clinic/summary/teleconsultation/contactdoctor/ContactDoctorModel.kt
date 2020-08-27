package org.simple.clinic.summary.teleconsultation.contactdoctor

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.summary.teleconsultation.sync.MedicalOfficer

@Parcelize
data class ContactDoctorModel(
    val medicalOfficers: List<MedicalOfficer>?
) : Parcelable {

  companion object {
    fun create() = ContactDoctorModel(
        medicalOfficers = null
    )
  }

  val hasMedicalOfficers: Boolean
    get() = medicalOfficers != null

  fun medicalOfficersLoaded(medicalOfficers: List<MedicalOfficer>): ContactDoctorModel {
    return copy(medicalOfficers = medicalOfficers)
  }
}
