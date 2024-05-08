package org.simple.clinic.reassignpatient

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class ReassignPatientSheetOpenedFrom : Parcelable {
  BACK_CLICK,
  DONE_CLICK,
}
