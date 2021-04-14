package org.simple.clinic.patient

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.patient.businessid.Identifier

sealed class PatientSearchCriteria(val additionalIdentifier: Identifier?) : Parcelable {

  @Parcelize
  data class Name(
      val patientName: String,
      private val _additionalIdentifier: Identifier? = null
  ) : PatientSearchCriteria(_additionalIdentifier)

  @Parcelize
  data class PhoneNumber(
      val phoneNumber: String,
      private val _additionalIdentifier: Identifier? = null
  ) : PatientSearchCriteria(_additionalIdentifier)
}
