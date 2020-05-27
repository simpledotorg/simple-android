package org.simple.clinic.editpatient.deletepatient

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class PatientDeleteReason : Parcelable {

  @Parcelize
  object Duplicate : PatientDeleteReason()

  @Parcelize
  object AccidentalRegistration : PatientDeleteReason()

  @Parcelize
  object Died : PatientDeleteReason()
}
