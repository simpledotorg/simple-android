package org.simple.clinic.reassignPatient

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class ReassignPatientSheetOpenedFrom : Parcelable {
  BACK_CLICK,
  DONE_CLICK,
}
