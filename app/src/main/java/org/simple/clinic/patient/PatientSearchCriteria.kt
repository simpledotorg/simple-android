package org.simple.clinic.patient

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class PatientSearchCriteria : Parcelable {

  @Parcelize
  data class Name(val patientName: String) : PatientSearchCriteria()

  @Parcelize
  data class PhoneNumber(val phoneNumber: String) : PatientSearchCriteria()
}
