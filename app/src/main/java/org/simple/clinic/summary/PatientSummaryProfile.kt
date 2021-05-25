package org.simple.clinic.summary

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.util.Unicode

@Parcelize
data class PatientSummaryProfile(
    val patient: Patient,
    val address: PatientAddress,
    val phoneNumber: PatientPhoneNumber?,
    val bpPassport: BusinessId?,
    val alternativeId: BusinessId?,
    val facility: Facility?
) : Parcelable {

  val hasPhoneNumber: Boolean
    get() = phoneNumber != null

  fun hasIdentifier(identifier: Identifier): Boolean {
    return bpPassport?.identifier == identifier || alternativeId?.identifier == identifier
  }

  override fun toString(): String {
    return "PatientSummaryProfile(${Unicode.redacted})"
  }
}
